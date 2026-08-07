package com.mruraza.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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
}