package com.mruraza.runtime_services.workflow

/** Static description of one step inside a workflow definition. */
data class WorkflowStep(
    val id: String,
    val name: String = id,
    /** Selects which registered [StepHandler] runs this step (e.g. "llm_call", "tool", "delay"). */
    val type: String,
    /** Handler-specific settings. Values may contain {{var}} / {{steps.otherStepId}} templates. */
    val config: Map<String, String> = emptyMap(),
    /** Step ids that must finish first. Steps with no unmet deps run in parallel. */
    val dependsOn: List<String> = emptyList(),
    val maxRetries: Int = 0,
    val retryBackoffMs: Long = 500,
    /** <= 0 means no timeout. */
    val timeoutMs: Long = 60_000,
    /** If true, a failure of this step doesn't fail the whole workflow. */
    val continueOnError: Boolean = false
)

enum class StepStatus { PENDING, RUNNING, COMPLETED, FAILED, SKIPPED, CANCELLED }

/** Runtime state of a step inside one [WorkflowInstance]. */
data class StepState(
    val status: StepStatus = StepStatus.PENDING,
    val attempts: Int = 0,
    val output: String? = null,
    val error: String? = null,
    val startedAt: Long? = null,
    val finishedAt: Long? = null
)

sealed interface StepResult {
    data class Success(
        val output: String = "",
        /** Merged into the instance variables so later steps can use {{name}}. */
        val variables: Map<String, String> = emptyMap()
    ) : StepResult

    data class Failure(
        val message: String,
        val retryable: Boolean = true
    ) : StepResult
}

/** What a handler sees while running. */
data class WorkflowContext(
    val instanceId: String,
    val variables: Map<String, String>,
    /** stepId -> output of every step completed so far. */
    val stepOutputs: Map<String, String>
) {
    private val template = Regex("\\{\\{\\s*([\\w.\\-]+)\\s*\\}\\}")

    /** Replaces {{var}} and {{steps.stepId}}; unknown keys are left as-is. */
    fun resolve(text: String): String = template.replace(text) { m ->
        val key = m.groupValues[1]
        val value = if (key.startsWith("steps.")) stepOutputs[key.removePrefix("steps.")] else variables[key]
        value ?: m.value
    }

    fun resolveConfig(config: Map<String, String>): Map<String, String> =
        config.mapValues { resolve(it.value) }
}

/** Implement (or pass a lambda) per step type and register it on the engine. */
fun interface StepHandler {
    suspend fun execute(step: WorkflowStep, context: WorkflowContext): StepResult
}