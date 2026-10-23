package com.mruraza.runtime_services.vector

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface VectorStore {
    suspend fun upsert(entry: VectorEntry)
    suspend fun upsertAll(entries: List<VectorEntry>)
    suspend fun get(id: String): VectorEntry?
    suspend fun delete(id: String)
    /** Deletes every entry whose metadata[key] == value. Returns how many were removed. */
    suspend fun deleteByMetadata(key: String, value: String): Int
    suspend fun getAll(): List<VectorEntry>
    suspend fun search(
        query: Embedding,
        topK: Int = 5,
        minScore: Float = 0f,
        filter: Map<String, String>? = null
    ): List<VectorSearchResult>
    suspend fun count(): Int
    suspend fun clear()
}

/**
 * Brute-force cosine search in RAM. Fine up to a few tens of thousands of vectors.
 * Replace with a Room/SQLite or ANN-index version later by rebinding in VectorModule.
 */
@Singleton
class InMemoryVectorStore @Inject constructor() : VectorStore {

    private val mutex = Mutex()
    private val entries = LinkedHashMap<String, VectorEntry>()

    override suspend fun upsert(entry: VectorEntry) = mutex.withLock { entries[entry.id] = entry; Unit }

    override suspend fun upsertAll(entries: List<VectorEntry>) = mutex.withLock {
        entries.forEach { this.entries[it.id] = it }
    }

    override suspend fun get(id: String): VectorEntry? = mutex.withLock { entries[id] }

    override suspend fun delete(id: String) = mutex.withLock { entries.remove(id); Unit }

    override suspend fun deleteByMetadata(key: String, value: String): Int = mutex.withLock {
        val ids = entries.values.filter { it.metadata[key] == value }.map { it.id }
        ids.forEach { entries.remove(it) }
        ids.size
    }

    override suspend fun getAll(): List<VectorEntry> = mutex.withLock { entries.values.toList() }

    override suspend fun search(
        query: Embedding, topK: Int, minScore: Float, filter: Map<String, String>?
    ): List<VectorSearchResult> = mutex.withLock {
        if (topK <= 0) return@withLock emptyList()
        entries.values.asSequence()
            .filter { it.embedding.dimension == query.dimension }
            .filter { e -> filter == null || filter.all { (k, v) -> e.metadata[k] == v } }
            .map { VectorSearchResult(it, it.embedding.cosine(query)) }
            .filter { it.score >= minScore }
            .sortedByDescending { it.score }
            .take(topK)
            .toList()
    }

    override suspend fun count(): Int = mutex.withLock { entries.size }

    override suspend fun clear() = mutex.withLock { entries.clear() }
}