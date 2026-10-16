package com.mruraza.runtime_services.memory

import com.mruraza.common.model.LongTermMemoEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Long-term memory with an in-RAM cache, write-through to [MemoryDataSource].
 * Reads/search run on the cache, so they're fast even when the source is Room.
 */
@Singleton
class MemoryRepo @Inject constructor(
    private val dataSource: MemoryDataSource
) : LongTermMemory {

    private val mutex = Mutex()
    private val cache = LinkedHashMap<String, LongTermMemoEntity>()
    private var loaded = false

    /** Must be called inside the mutex. */
    private suspend fun ensureLoaded() {
        if (loaded) return
        dataSource.getAll().forEach { cache[it.memoryId] = it }
        loaded = true
    }

    override suspend fun save(memory: LongTermMemoEntity) = mutex.withLock {
        ensureLoaded()
        cache[memory.memoryId] = memory
        dataSource.save(memory)
    }

    override suspend fun get(memoryId: String): LongTermMemoEntity? = mutex.withLock {
        ensureLoaded()
        cache[memoryId]
    }

    override suspend fun delete(memoryId: String) = mutex.withLock {
        ensureLoaded()
        cache.remove(memoryId)
        dataSource.delete(memoryId)
    }

    override suspend fun getImportant(limit: Int): List<LongTermMemoEntity> = mutex.withLock {
        ensureLoaded()
        val now = System.currentTimeMillis()
        cache.values
            .sortedByDescending { MemoryScorer.importanceScore(it, now) }
            .take(limit.coerceAtLeast(0))
    }

    override suspend fun updateAccess(memoryId: String) = mutex.withLock {
        ensureLoaded()
        val old = cache[memoryId] ?: return@withLock
        val updated = old.copy(
            lastAccessedAt = System.currentTimeMillis(),
            accessCount = old.accessCount + 1
        )
        cache[memoryId] = updated
        dataSource.save(updated)
    }

    override suspend fun search(query: String, limit: Int): List<LongTermMemoEntity> = mutex.withLock {
        ensureLoaded()
        val tokens = MemoryScorer.tokenize(query)
        val now = System.currentTimeMillis()
        cache.values
            .map { it to MemoryScorer.score(it, tokens, now) }
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }
            .take(limit.coerceAtLeast(0))
            .map { it.first }
    }

    override suspend fun getByTag(tag: String, limit: Int): List<LongTermMemoEntity> = mutex.withLock {
        ensureLoaded()
        cache.values
            .filter { m -> m.tags.any { it.equals(tag, ignoreCase = true) } }
            .sortedByDescending { it.importance }
            .take(limit.coerceAtLeast(0))
    }

    override suspend fun getRecent(limit: Int): List<LongTermMemoEntity> = mutex.withLock {
        ensureLoaded()
        cache.values.sortedByDescending { it.createdAt }.take(limit.coerceAtLeast(0))
    }

    override suspend fun getAll(): List<LongTermMemoEntity> = mutex.withLock {
        ensureLoaded()
        cache.values.toList()
    }

    override suspend fun count(): Int = mutex.withLock {
        ensureLoaded()
        cache.size
    }

    override suspend fun clear() = mutex.withLock {
        cache.clear()
        dataSource.clear()
        loaded = true
    }
}