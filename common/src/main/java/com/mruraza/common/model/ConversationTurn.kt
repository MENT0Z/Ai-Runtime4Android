package com.mruraza.common.model

data class ConversationTurn(
    val createdAt: Long,
    val role: ConversationRole,
    val content: String
) {
    enum class ConversationRole {
        SYSTEM,
        USER,
        ASSISTANT,
        TOOL
    }
}







