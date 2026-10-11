package com.mruraza.runtime_services.memory

import com.mruraza.common.model.LongTermMemoEntity

interface LongTermMemory{
    suspend fun save(memory: LongTermMemoEntity)
    suspend fun get(memoryId: String): LongTermMemoEntity?
    suspend fun delete(memoryId: String)
    suspend fun getImportant(limit: Int = 20): List<LongTermMemoEntity>
    suspend fun updateAccess(memoryId: String)
}