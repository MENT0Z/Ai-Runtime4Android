package com.mruraza.common.model

data class AgentSession(
    val sessionId: String,
    val status: SessionStatus,
    val createdAt: Long,
    val metadata: String // json blob
) {
    enum class SessionStatus {
        ACTIVE,
        PAUSED,
        EXPIRED
    }
}

