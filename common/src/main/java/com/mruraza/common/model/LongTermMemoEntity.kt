package com.mruraza.common.model

data class LongTermMemoEntity(
    val memoryId: String,
    val content: String,
    val type: MemoryType,
    val importance: Float,
    val createdAt: Long,
    val lastAccessedAt: Long,
    val accessCount: Int = 0
){
    enum class MemoryType{
         FACT,
         PREFERENCE,
         EVENT,
         PATTERN
    }
}
