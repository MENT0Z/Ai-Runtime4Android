package com.mruraza.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mruraza.common.model.AgentSession
import java.util.UUID

@Entity(tableName = "agent_sessions")
data class AgentSessionEntity(
    @PrimaryKey
    val sessionId: String = UUID.randomUUID().toString(),
    val status: SessionStatus = SessionStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val metadata: String = "{}" // json blob for future extensibility
) {
    enum class SessionStatus {
        ACTIVE,
        PAUSED,
        EXPIRED
    }

    fun toDomain(): AgentSession = AgentSession(
        sessionId = sessionId,
        status = AgentSession.SessionStatus.valueOf(status.name),
        createdAt = createdAt,
        metadata = metadata
    )

    companion object {
        fun fromDomain(agentSession: AgentSession): AgentSessionEntity = AgentSessionEntity(
            sessionId = agentSession.sessionId,
            status = SessionStatus.valueOf(agentSession.status.name),
            createdAt = agentSession.createdAt,
            metadata = agentSession.metadata
        )
    }
}