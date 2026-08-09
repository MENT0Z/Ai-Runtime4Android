package com.mruraza.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "conversation_history")
data class ConversationTurn(
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
}

