package com.mruraza.common.model

data class WorkflowState(
    val workflowId: String,
    val name: String,
    val currentStep: String,
    val status: WorkflowStatus,
    val data: Map<String, Any?> = emptyMap()
)

enum class WorkflowStatus {
    NOT_STARTED,
    IN_PROGRESS,
    WAITING_FOR_USER,
    COMPLETED,
    FAILED,
    CANCELLED
}