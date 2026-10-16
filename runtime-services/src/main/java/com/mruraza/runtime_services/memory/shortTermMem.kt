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

    suspend fun clearAll()

    suspend fun getSessionIds(): List<String>

    suspend fun size(sessionId: String): Int

    /** Removes and returns the last turn (useful for "regenerate" / undo). */
    suspend fun removeLast(sessionId: String): ConversationTurn?
}