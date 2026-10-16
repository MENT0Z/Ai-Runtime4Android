package com.mruraza.runtime_services.memory


import com.mruraza.common.model.LongTermMemoEntity
import javax.inject.Inject
import javax.inject.Singleton

// TODO Need to swap this for Room

/**
 * The "something called save" seam. Later: implement this with Room (DAO) and
 * change the binding in MemoryModule. Nothing else needs to change.
 */
interface MemoryDataSource {
    suspend fun save(memory: LongTermMemoEntity)
    suspend fun get(memoryId: String): LongTermMemoEntity?
    suspend fun delete(memoryId: String)
    suspend fun getAll(): List<LongTermMemoEntity>
    suspend fun clear()
}

/** Placeholder until Room exists. Data is lost when the process dies. */
@Singleton
class InMemoryMemoryDataSource @Inject constructor() : MemoryDataSource {
    private val store = HashMap<String, LongTermMemoEntity>()

    override suspend fun save(memory: LongTermMemoEntity) { synchronized(store) { store[memory.memoryId] = memory } }
    override suspend fun get(memoryId: String) = synchronized(store) { store[memoryId] }
    override suspend fun delete(memoryId: String) { synchronized(store) { store.remove(memoryId) } }
    override suspend fun getAll() = synchronized(store) { store.values.toList() }
    override suspend fun clear() { synchronized(store) { store.clear() } }
}
