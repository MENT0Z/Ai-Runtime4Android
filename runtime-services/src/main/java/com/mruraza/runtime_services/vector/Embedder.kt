package com.mruraza.runtime_services.vector

import javax.inject.Inject
import javax.inject.Singleton

/** Turns text into vectors. Swap the implementation for a real model (TFLite MiniLM, ONNX, API...). */
interface Embedder {
    val dimension: Int
    suspend fun embed(text: String): Embedding
    suspend fun embedBatch(texts: List<String>): List<Embedding> = texts.map { embed(it) }
}

/**
 * Zero-dependency placeholder: feature hashing of words + character trigrams.
 * Catches shared words and similar spellings, NOT synonyms. Good enough to build and test the
 * whole pipeline; replace with a neural embedder later and call VectorService.reindexAll().
 */
@Singleton
class HashingEmbedder @Inject constructor() : Embedder {

    override val dimension: Int = 256
    private val splitter = Regex("[^\\p{L}\\p{N}]+")

    override suspend fun embed(text: String): Embedding {
        val v = FloatArray(dimension)
        val tokens = text.lowercase().split(splitter).filter { it.isNotEmpty() }
        for (t in tokens) {
            add(v, t, 1f)
            if (t.length >= 3) {
                val padded = "#$t#"
                for (i in 0..padded.length - 3) add(v, padded.substring(i, i + 3), 0.5f)
            }
        }
        return Embedding(v).normalized()
    }

    private fun add(v: FloatArray, feature: String, weight: Float) {
        val h = fnv1a(feature)
        val idx = (h and 0x7fffffff) % dimension
        val sign = if (((h ushr 16) and 1) == 0) 1f else -1f
        v[idx] += sign * weight
    }

    private fun fnv1a(s: String): Int {
        var h = 0x811c9dc5.toInt()
        for (c in s) {
            h = h xor c.code
            h *= 16777619
        }
        return h
    }
}