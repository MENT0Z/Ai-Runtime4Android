package com.mruraza.runtime_services.workflow

/** Emitted by [WorkflowEngine.events] so UI / logging can observe progress. */
sealed interface WorkflowEvent {
    val instanceId: String

    data class InstanceStarted(override val instanceId: String) : WorkflowEvent
    data class StepStarted(override val instanceId: String, val stepId: String) : WorkflowEvent
    data class StepFinished(
        override val instanceId: String,
        val stepId: String,
        val status: StepStatus,
        val output: String?,
        val error: String?
    ) : WorkflowEvent
    data class InstanceFinished(
        override val instanceId: String,
        val status: WorkflowStatus,
        val error: String?
    ) : WorkflowEvent
}