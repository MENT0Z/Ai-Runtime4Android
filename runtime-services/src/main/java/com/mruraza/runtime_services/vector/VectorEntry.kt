package com.mruraza.runtime_services.vector

/** One stored item: original text + its vector + free-form metadata (source, docId, type...). */
data class VectorEntry(
    val id: String,
    val text: String,
    val embedding: Embedding,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

data class VectorSearchResult(
    val entry: VectorEntry,
    val score: Float
)