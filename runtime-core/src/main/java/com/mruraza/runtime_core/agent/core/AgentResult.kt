package com.mruraza.runtime_core.agent.core

sealed interface AgentResult{
    val taskId: String

    data class Success(
        override val taskId: String,
        val output:Any? = null,
        val metadata: Map<String, Any?> = emptyMap()
    ):AgentResult

    data class Failure(
        override val taskId: String,
        val error: String,
        val retryable: Boolean = false
    ) : AgentResult

    data class Cancelled(
        override val taskId: String,
        val reason: String? = null
    ) : AgentResult

    data class Timeout(
        override val taskId: String,
        val timeoutMillis: Long
    ) : AgentResult
}