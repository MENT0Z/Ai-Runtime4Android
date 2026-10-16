package com.mruraza.runtime_services.memory

import com.mruraza.common.model.LongTermMemoEntity
import kotlin.math.exp

/**
 * Lightweight keyword relevance scoring (no embeddings needed).
 * Later you can swap this for vector search without touching callers.
 */
object MemoryScorer {

    private val STOP_WORDS = setOf(
        "the", "and", "for", "are", "but", "not", "you", "your", "with", "this", "that",
        "have", "has", "was", "were", "what", "when", "where", "who", "how", "why",
        "can", "could", "would", "should", "about", "from", "into", "than", "then",
        "she", "his", "her", "him", "its", "our", "out", "all", "any", "get", "got"
    )
    private val SPLITTER = Regex("[^\\p{L}\\p{N}]+")
    private const val DAY_MS = 86_400_000.0

    fun tokenize(text: String): Set<String> =
        text.lowercase().split(SPLITTER)
            .filter { it.length > 2 && it !in STOP_WORDS }
            .toSet()

    /** 0.0 means "not relevant" – callers should drop those. */
    // TODO need to implement semantic meaning too
    fun score(
        memory: LongTermMemoEntity,
        queryTokens: Set<String>,
        now: Long = System.currentTimeMillis()
    ): Double {
        if (queryTokens.isEmpty()) return 0.0
        val memTokens = tokenize(memory.content + " " + memory.tags.joinToString(" "))
        val overlap = queryTokens.count { it in memTokens }
        if (overlap == 0) return 0.0

        val relevance = overlap.toDouble() / queryTokens.size
        val importance = memory.importance.coerceIn(0f, 1f).toDouble()
        val ageDays = ((now - memory.lastAccessedAt).coerceAtLeast(0)) / DAY_MS
        val recency = exp(-ageDays / 30.0)           // ~1 today, ~0.37 after 30 days

        return 0.60 * relevance + 0.25 * importance + 0.15 * recency
    }

    /** Ranking when there is no query: importance first, then recency. */
    fun importanceScore(memory: LongTermMemoEntity, now: Long = System.currentTimeMillis()): Double {
        val ageDays = ((now - memory.lastAccessedAt).coerceAtLeast(0)) / DAY_MS
        return 0.8 * memory.importance.coerceIn(0f, 1f) + 0.2 * exp(-ageDays / 30.0)
    }
}