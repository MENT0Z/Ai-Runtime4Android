package com.mruraza.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mruraza.common.model.ConversationTurn
import java.util.UUID

@Entity(tableName = "conversation_history")
data class ConversationTurnEntity(
    @PrimaryKey(autoGenerate = true)
    val conversationId: Long = 0,
    val sessionId: String = UUID.randomUUID().toString(), // later maps to the correct one
    val createdAt: Long = System.currentTimeMillis(),
    val role: ConversationRole = ConversationRole.USER,
    val content: String = ""
) {
    enum class ConversationRole {
        SYSTEM,
        USER,
        ASSISTANT,
        TOOL
    }

    fun toDomain(): ConversationTurn {
        return ConversationTurn(
            conversationId = conversationId,
            sessionId = sessionId,
            createdAt = createdAt,
            role = ConversationTurn.ConversationRole.valueOf(role.name),
            content = content
        )
    }

    companion object{
        fun fromDomain(domain: ConversationTurn): ConversationTurnEntity {
            return ConversationTurnEntity(
                conversationId = domain.conversationId,
                sessionId = domain.sessionId,
                createdAt = domain.createdAt,
                role = ConversationRole.valueOf(domain.role.name),
                content = domain.content
            )
        }
    }
}

