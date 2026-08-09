package com.mruraza.common.model

data class MemoryEntry(
    val id: String,
    val content: String,
    val relevanceScore: Double? = null,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long? = null
)
