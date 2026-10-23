package com.mruraza.runtime_services.workflow

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs workflow DAGs: parallel-where-possible, with retries, timeouts, cancel and resume.
 *
 * Usage:
 *   engine.registerHandler("llm_call") { step, ctx -> StepResult.Success(output = "...") }
 *   engine.registerDefinition(def)
 *   val id = engine.start(def.id, mapOf("topic" to "Kotlin"))
 *   val result = engine.await(id)
 */
@Singleton
class WorkflowEngine @Inject constructor(
    private val repo: WorkflowRepo
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val handlers = ConcurrentHashMap<String, StepHandler>()
    private val jobs = ConcurrentHashMap<String, Job>()
    private val stateMutex = Mutex()

    private val _events = MutableSharedFlow<WorkflowEvent>(extraBufferCapacity = 128)
    val events: SharedFlow<WorkflowEvent> = _events.asSharedFlow()

    init {
        // Built-in handlers
        registerHandler("noop") { _, _ -> StepResult.Success() }
        registerHandler("delay") { step, ctx ->
            val ms = ctx.resolve(step.config["ms"] ?: "0").toLongOrNull() ?: 0L
            delay(ms)
            StepResult.Success(output = "waited ${ms}ms")
        }
        registerHandler("set_variable") { step, ctx ->
            val key = step.config["key"] ?: return@registerHandler StepResult.Failure("config.key missing", retryable = false)
            val value = ctx.resolve(step.config["value"] ?: "")
            StepResult.Success(output = value, variables = mapOf(key to value))
        }
    }

    // ------------------------------------------------------------------ setup

    fun registerHandler(type: String, handler: StepHandler) { handlers[type] = handler }

    suspend fun registerDefinition(definition: WorkflowDefinition) {
        val errors = definition.validate()
        require(errors.isEmpty()) { "Invalid workflow '${definition.id}': ${errors.joinToString("; ")}" }
        repo.saveDefinition(definition)
    }

    // ------------------------------------------------------------------ control

    /** Starts a run in the background and returns the instance id immediately. */
    suspend fun start(definitionId: String, variables: Map<String, String> = emptyMap()): String {
        val def = repo.getDefinition(definitionId)
            ?: throw IllegalArgumentException("Unknown workflow: $definitionId")
        val missing = def.steps
            .map { it.type }
            .distinct()
            .filter { !handlers.containsKey(it) }

        require(missing.isEmpty()) {
            "No handler registered for step types: $missing"
        }

        val instance = WorkflowInstance(
            definitionId = definitionId,
            stepStates = def.steps.associate { it.id to StepState() },
            variables = variables
        )
        repo.saveInstance(instance)
        launchRun(instance.id)
        return instance.id
    }

    suspend fun startAndAwait(definitionId: String, variables: Map<String, String> = emptyMap()): WorkflowInstance =
        await(start(definitionId, variables))

    /** Re-runs a FAILED / CANCELLED instance; completed steps are kept and not repeated. */
    suspend fun resume(instanceId: String): Boolean {
        if (jobs.containsKey(instanceId)) return false
        val updated = update(instanceId) { inst ->
            if (inst.status != WorkflowStatus.FAILED && inst.status != WorkflowStatus.CANCELLED) inst
            else inst.copy(
                status = WorkflowStatus.PENDING, error = null, finishedAt = null,
                stepStates = inst.stepStates.mapValues { (_, s) ->
                    if (s.status == StepStatus.COMPLETED) s else StepState()
                }
            )
        } ?: return false
        if (updated.status != WorkflowStatus.PENDING) return false
        launchRun(instanceId)
        return true
    }

    suspend fun cancel(instanceId: String) {
        val job = jobs[instanceId]
        if (job != null) {
            job.cancel()
            job.join()
        }
    }

    /** Suspends until the instance reaches a terminal state, then returns it. */
    suspend fun await(instanceId: String): WorkflowInstance {
        jobs[instanceId]?.join()
        return repo.getInstance(instanceId)
            ?: throw IllegalArgumentException("Unknown instance: $instanceId")
    }

    suspend fun getInstance(instanceId: String) = repo.getInstance(instanceId)
    fun isRunning(instanceId: String) = jobs[instanceId]?.isActive == true

    // ------------------------------------------------------------------ running

    private fun launchRun(instanceId: String) {
        val job = scope.launch(start = CoroutineStart.LAZY) { run(instanceId) }
        jobs[instanceId] = job
        job.invokeOnCompletion { jobs.remove(instanceId, job) }
        job.start()
    }

    private suspend fun run(instanceId: String) {
        val first = repo.getInstance(instanceId) ?: return
        val def = repo.getDefinition(first.definitionId)
        if (def == null) {
            finish(instanceId, WorkflowStatus.FAILED, "Definition ${first.definitionId} not found")
            return
        }
        val stepById = def.steps.associateBy { it.id }

        update(instanceId) {
            it.copy(status = WorkflowStatus.RUNNING, startedAt = it.startedAt ?: now(), finishedAt = null, error = null)
        }
        _events.tryEmit(WorkflowEvent.InstanceStarted(instanceId))

        try {
            while (true) {
                val inst = repo.getInstance(instanceId) ?: return
                val pending = def.steps.filter { inst.stepStates[it.id]?.status == StepStatus.PENDING }
                if (pending.isEmpty()) break

                val ready = pending.filter { s ->
                    s.dependsOn.all { depId ->
                        val st = inst.stepStates[depId]?.status
                        st == StepStatus.COMPLETED || st == StepStatus.SKIPPED ||
                                (st == StepStatus.FAILED && stepById[depId]?.continueOnError == true)
                    }
                }
                if (ready.isEmpty()) {
                    // Remaining steps are blocked by a failed dependency
                    skipAllPending(instanceId)
                    break
                }

                coroutineScope { ready.map { step -> async { runStep(instanceId, step) } }.awaitAll() }

                val after = repo.getInstance(instanceId) ?: return
                val fatal = def.steps.any { s ->
                    after.stepStates[s.id]?.status == StepStatus.FAILED && !s.continueOnError
                }
                if (fatal) {
                    skipAllPending(instanceId)
                    break
                }
            }

            val done = repo.getInstance(instanceId) ?: return
            val firstFatal = def.steps.firstOrNull { s ->
                done.stepStates[s.id]?.status == StepStatus.FAILED && !s.continueOnError
            }
            if (firstFatal != null) {
                finish(instanceId, WorkflowStatus.FAILED,
                    "Step '${firstFatal.id}' failed: ${done.stepStates[firstFatal.id]?.error}")
            } else {
                finish(instanceId, WorkflowStatus.COMPLETED, null)
            }
        } catch (e: CancellationException) {
            withContext(NonCancellable) {
                update(instanceId) { inst ->
                    inst.copy(stepStates = inst.stepStates.mapValues { (_, s) ->
                        if (s.status == StepStatus.RUNNING || s.status == StepStatus.PENDING)
                            s.copy(status = StepStatus.CANCELLED, finishedAt = now()) else s
                    })
                }
                finish(instanceId, WorkflowStatus.CANCELLED, "Cancelled")
            }
            throw e
        } catch (e: Exception) {
            finish(instanceId, WorkflowStatus.FAILED, e.message ?: e::class.java.simpleName)
        }
    }

    private suspend fun runStep(instanceId: String, step: WorkflowStep) {
        val handler = handlers[step.type]
        if (handler == null) {
            setStep(instanceId, step.id) {
                it.copy(status = StepStatus.FAILED, error = "No handler for type '${step.type}'", finishedAt = now())
            }
            _events.tryEmit(WorkflowEvent.StepFinished(instanceId, step.id, StepStatus.FAILED, null, "No handler"))
            return
        }

        setStep(instanceId, step.id) { it.copy(status = StepStatus.RUNNING, startedAt = now(), error = null) }
        _events.tryEmit(WorkflowEvent.StepStarted(instanceId, step.id))

        val maxAttempts = step.maxRetries.coerceAtLeast(0) + 1
        var attempt = 0
        var lastError = "Unknown error"

        while (attempt < maxAttempts) {
            attempt++
            setStep(instanceId, step.id) { it.copy(attempts = attempt) }

            val inst = repo.getInstance(instanceId) ?: return
            val ctx = WorkflowContext(
                instanceId = instanceId,
                variables = inst.variables,
                stepOutputs = inst.stepStates.mapNotNull { (k, v) -> v.output?.let { k to it } }.toMap()
            )

            val result: StepResult = try {
                if (step.timeoutMs > 0) {
                    withTimeoutOrNull(step.timeoutMs) { handler.execute(step, ctx) }
                        ?: StepResult.Failure("Timed out after ${step.timeoutMs}ms")
                } else {
                    handler.execute(step, ctx)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                StepResult.Failure(e.message ?: e::class.java.simpleName)
            }

            when (result) {
                is StepResult.Success -> {
                    update(instanceId) { i ->
                        i.copy(
                            variables = i.variables + result.variables,
                            stepStates = i.stepStates + (step.id to (i.stepStates[step.id] ?: StepState()).copy(
                                status = StepStatus.COMPLETED, output = result.output,
                                error = null, finishedAt = now()
                            ))
                        )
                    }
                    _events.tryEmit(WorkflowEvent.StepFinished(instanceId, step.id, StepStatus.COMPLETED, result.output, null))
                    return
                }
                is StepResult.Failure -> {
                    lastError = result.message
                    if (!result.retryable || attempt >= maxAttempts) break
                    val backoff = (step.retryBackoffMs * (1L shl (attempt - 1).coerceAtMost(10))).coerceAtMost(30_000L)
                    delay(backoff)
                }
            }
        }

        setStep(instanceId, step.id) { it.copy(status = StepStatus.FAILED, error = lastError, finishedAt = now()) }
        _events.tryEmit(WorkflowEvent.StepFinished(instanceId, step.id, StepStatus.FAILED, null, lastError))
    }

    // ------------------------------------------------------------------ helpers

    private fun now() = System.currentTimeMillis()

    private suspend fun update(id: String, transform: (WorkflowInstance) -> WorkflowInstance): WorkflowInstance? =
        stateMutex.withLock {
            val current = repo.getInstance(id) ?: return@withLock null
            transform(current).also { repo.saveInstance(it) }
        }

    private suspend fun setStep(id: String, stepId: String, transform: (StepState) -> StepState) {
        update(id) { i ->
            i.copy(stepStates = i.stepStates + (stepId to transform(i.stepStates[stepId] ?: StepState())))
        }
    }

    private suspend fun skipAllPending(id: String) {
        update(id) { i ->
            i.copy(stepStates = i.stepStates.mapValues { (_, s) ->
                if (s.status == StepStatus.PENDING) s.copy(status = StepStatus.SKIPPED, finishedAt = now()) else s
            })
        }
    }

    private suspend fun finish(id: String, status: WorkflowStatus, error: String?) {
        update(id) { it.copy(status = status, error = error, finishedAt = now()) }
        _events.tryEmit(WorkflowEvent.InstanceFinished(id, status, error))
    }

    // Convenience for building definitions in code
    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}