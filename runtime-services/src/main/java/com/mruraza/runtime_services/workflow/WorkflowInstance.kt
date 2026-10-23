package com.mruraza.runtime_services.workflow

import java.util.UUID

enum class WorkflowStatus {
    PENDING, RUNNING, COMPLETED, FAILED, CANCELLED;

    val isTerminal: Boolean get() = this == COMPLETED || this == FAILED || this == CANCELLED
}

/** One execution of a [WorkflowDefinition]. */
data class WorkflowInstance(
    val id: String = UUID.randomUUID().toString(),
    val definitionId: String,
    val status: WorkflowStatus = WorkflowStatus.PENDING,
    val stepStates: Map<String, StepState>,
    val variables: Map<String, String> = emptyMap(),
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val finishedAt: Long? = null
) {
    fun stepOutput(stepId: String): String? = stepStates[stepId]?.output
    val progress: Float
        get() {
            if (stepStates.isEmpty()) return 0f
            val done = stepStates.values.count {
                it.status == StepStatus.COMPLETED || it.status == StepStatus.SKIPPED || it.status == StepStatus.FAILED
            }
            return done.toFloat() / stepStates.size
        }
}