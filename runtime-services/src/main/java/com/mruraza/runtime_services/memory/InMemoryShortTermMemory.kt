package com.mruraza.runtime_services.memory

import com.mruraza.common.model.ConversationTurn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RAM-only short-term memory. Nothing is persisted; it dies with the process.
 * Each session keeps at most [maxTurnsPerSession] turns (oldest dropped first).
 */
@Singleton
class InMemoryShortTermMemory @Inject constructor() : ShortTermMemory {

    private val maxTurnsPerSession = 50
    private val mutex = Mutex()
    private val sessions = HashMap<String, ArrayDeque<ConversationTurn>>()

    override suspend fun add(turn: ConversationTurn) = mutex.withLock {
        val deque = sessions.getOrPut(turn.sessionId) { ArrayDeque() }
        deque.addLast(turn)
        while (deque.size > maxTurnsPerSession) deque.removeFirst()
    }

    override suspend fun getRecentConversations(
        sessionId: String,
        limit: Int
    ): List<ConversationTurn> = mutex.withLock {
        if (limit <= 0) return@withLock emptyList()
        sessions[sessionId]?.toList()?.takeLast(limit) ?: emptyList()
    }

    override suspend fun clear(sessionId: String) = mutex.withLock {
        sessions.remove(sessionId)
        Unit
    }

    override suspend fun clearAll() = mutex.withLock { sessions.clear() }

    override suspend fun getSessionIds(): List<String> = mutex.withLock { sessions.keys.toList() }

    override suspend fun size(sessionId: String): Int = mutex.withLock { sessions[sessionId]?.size ?: 0 }

    override suspend fun removeLast(sessionId: String): ConversationTurn? = mutex.withLock {
        sessions[sessionId]?.removeLastOrNull()
    }
}