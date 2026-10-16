package com.mruraza.runtime_services.memory

import com.mruraza.common.model.ConversationTurn
import com.mruraza.common.model.LongTermMemoEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for the ContextManager. Call [buildContext] before building a prompt.
 */
@Singleton
class MemoryServices @Inject constructor(
    private val shortTermMemory: ShortTermMemory,
    private val longTermMemory: LongTermMemory
) {

    // ---------- long-term ----------

    /** Save a ready-made memory. Blank content is ignored. */
    suspend fun remember(memory: LongTermMemoEntity) {
        if (memory.content.isBlank()) return
        longTermMemory.save(memory)
    }

    /** Convenience: create + save a memory from plain text. Returns its id. */
    suspend fun remember(
        content: String,
        importance: Float = 0.5f,
        type: LongTermMemoEntity.MemoryType = LongTermMemoEntity.MemoryType.FACT,
        tags: List<String> = emptyList()
    ): String? {
        if (content.isBlank()) return null
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        longTermMemory.save(
            LongTermMemoEntity(
                memoryId = id,
                content = content.trim(),
                tags = tags,
                importance = importance.coerceIn(0f, 1f),
                type = type,
                createdAt = now,
                lastAccessedAt = now,
                accessCount = 0
            )
        )
        return id
    }

    /** Fetch by id and mark it as accessed. */
    suspend fun recall(memoryId: String): LongTermMemoEntity? {
        val memory = longTermMemory.get(memoryId) ?: return null
        longTermMemory.updateAccess(memoryId)
        return memory
    }

    /** Memories related to [query] (keyword match + importance + recency). */
    suspend fun recallRelevant(query: String, limit: Int = 5): List<LongTermMemoEntity> {
        if (query.isBlank()) return emptyList()
        val results: List<LongTermMemoEntity> = longTermMemory.search(query, limit)
        results.forEach { longTermMemory.updateAccess(it.memoryId) }
        return results
    }

    suspend fun getImportantMemories(limit: Int = 10) = longTermMemory.getImportant(limit)
    suspend fun recallByTag(tag: String, limit: Int = 20) = longTermMemory.getByTag(tag, limit)
    suspend fun forget(memoryId: String) = longTermMemory.delete(memoryId)
    suspend fun forgetAll() = longTermMemory.clear()

    // ---------- short-term ----------

    suspend fun addTurn(turn: ConversationTurn) = shortTermMemory.add(turn)

    suspend fun getRecentConversation(
        sessionId: String,
        limit: Int = 20
    ): List<ConversationTurn> = shortTermMemory.getRecentConversations(sessionId, limit)

    suspend fun clearSession(sessionId: String) = shortTermMemory.clear(sessionId)

    /** Drop the last turn, e.g. when the user hits "regenerate". */
    suspend fun undoLastTurn(sessionId: String) = shortTermMemory.removeLast(sessionId)

    // ---------- for ContextManager ----------

    /**
     * One call that gathers everything: recent chat + memories relevant to [userQuery]
     * + top important memories.
     */
// TODO Need to call this in the context manager
//    suspend fun buildContext(
//        sessionId: String,
//        userQuery: String,
//        recentLimit: Int = 20,
//        relevantLimit: Int = 5,
//        importantLimit: Int = 5
//    ): MemoryContext {
//        val recent = shortTermMemory.getRecentConversations(sessionId, recentLimit)
//        val relevant = recallRelevant(userQuery, relevantLimit)
//        val relevantIds = relevant.map { it.id }.toSet()
//        val important = longTermMemory.getImportant(importantLimit + relevantIds.size)
//            .filter { it.id !in relevantIds }
//            .take(importantLimit)
//        return MemoryContext(recent, relevant, important)
//    }
}