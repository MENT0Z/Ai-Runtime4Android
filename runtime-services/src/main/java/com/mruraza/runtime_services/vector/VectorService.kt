package com.mruraza.runtime_services.vector

import javax.inject.Inject
import javax.inject.Singleton

data class VectorItem(
    val id: String,
    val text: String,
    val metadata: Map<String, String> = emptyMap()
)

/** Main entry point: index text, then search by meaning/similarity. */
@Singleton
class VectorService @Inject constructor(
    private val embedder: Embedder,
    private val store: VectorStore
) {

    suspend fun index(id: String, text: String, metadata: Map<String, String> = emptyMap()): VectorEntry? {
        if (text.isBlank()) return null
        val entry = VectorEntry(id, text.trim(), embedder.embed(text), metadata)
        store.upsert(entry)
        return entry
    }

    suspend fun indexAll(items: List<VectorItem>) {
        val valid = items.filter { it.text.isNotBlank() }
        if (valid.isEmpty()) return
        val vectors = embedder.embedBatch(valid.map { it.text })
        store.upsertAll(valid.mapIndexed { i, it ->
            VectorEntry(it.id, it.text.trim(), vectors[i], it.metadata)
        })
    }

    /**
     * RAG helper: split a long document into chunks and index them.
     * Re-indexing the same [docId] replaces its old chunks. Returns chunk ids.
     */
    suspend fun indexDocument(
        docId: String,
        text: String,
        metadata: Map<String, String> = emptyMap(),
        chunkSize: Int = 500,
        overlap: Int = 50
    ): List<String> {
        store.deleteByMetadata("docId", docId)
        val chunks = TextChunker.chunk(text, chunkSize, overlap)
        val items = chunks.mapIndexed { i, c ->
            VectorItem("$docId#$i", c, metadata + mapOf("docId" to docId, "chunkIndex" to i.toString()))
        }
        indexAll(items)
        return items.map { it.id }
    }

    suspend fun search(
        query: String,
        topK: Int = 5,
        minScore: Float = 0.1f,
        filter: Map<String, String>? = null
    ): List<VectorSearchResult> {
        if (query.isBlank()) return emptyList()
        return store.search(embedder.embed(query), topK, minScore, filter)
    }

    /** Items most similar to an already-indexed item (excluding itself). */
    suspend fun findSimilar(id: String, topK: Int = 5, minScore: Float = 0.1f): List<VectorSearchResult> {
        val source = store.get(id) ?: return emptyList()
        return store.search(source.embedding, topK + 1, minScore).filter { it.entry.id != id }.take(topK)
    }

    suspend fun get(id: String) = store.get(id)
    suspend fun remove(id: String) = store.delete(id)
    suspend fun removeDocument(docId: String) = store.deleteByMetadata("docId", docId)
    suspend fun count() = store.count()
    suspend fun clear() = store.clear()

    /** Re-embed everything (call after switching to a different Embedder). */
    suspend fun reindexAll() {
        val all = store.getAll()
        if (all.isEmpty()) return
        val vectors = embedder.embedBatch(all.map { it.text })
        store.upsertAll(all.mapIndexed { i, e -> e.copy(embedding = vectors[i]) })
    }

    /** Search + format as a prompt block for the ContextManager. */
    suspend fun buildPrompt(
        query: String,
        topK: Int = 3,
        minScore: Float = 0.15f,
        maxChars: Int = 2000,
        filter: Map<String, String>? = null
    ): String {
        val results = search(query, topK, minScore, filter)
        if (results.isEmpty()) return ""
        val sb = StringBuilder("Relevant information:\n")
        for (r in results) {
            val line = "- ${r.entry.text}\n"
            if (sb.length + line.length > maxChars) break
            sb.append(line)
        }
        return sb.toString().trimEnd()
    }
}