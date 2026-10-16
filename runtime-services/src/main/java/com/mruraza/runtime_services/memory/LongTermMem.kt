package com.mruraza.runtime_services.memory

import com.mruraza.common.model.LongTermMemoEntity

interface LongTermMemory{
    suspend fun save(memory: LongTermMemoEntity)
    suspend fun get(memoryId: String): LongTermMemoEntity?
    suspend fun delete(memoryId: String)
    suspend fun getImportant(limit: Int = 20): List<LongTermMemoEntity>
    suspend fun updateAccess(memoryId: String)
    suspend fun search(query: String, limit: Int = 5): List<LongTermMemoEntity>
    // to get by tags of the query
    suspend fun getByTag(tag: String, limit: Int = 5): List<LongTermMemoEntity>
    suspend fun getRecent(limit: Int = 20) : List<LongTermMemoEntity>
    suspend fun getAll(): List<LongTermMemoEntity>
    suspend fun count(): Int
    suspend fun clear()
}