package com.mruraza.runtime_services.memory

import com.mruraza.common.model.ConversationTurn

interface ShortTermMemory{
    suspend fun add(turn: ConversationTurn)

    suspend fun getRecentConversations(
        sessionId: String,
        limit: Int = 20
    ):List<ConversationTurn>

    suspend fun clear(
        sessionId: String
    )
}