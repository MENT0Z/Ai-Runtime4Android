package com.mruraza.common.model

import java.util.UUID

data class ConversationTurn(
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





