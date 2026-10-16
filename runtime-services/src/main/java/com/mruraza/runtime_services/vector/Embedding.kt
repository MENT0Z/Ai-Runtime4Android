package com.mruraza.runtime_services.vector

import kotlin.math.sqrt

/** A dense vector. Compare with [cosine]; normalize once and [dot] is enough. */
class Embedding(val values: FloatArray) {

    val dimension: Int get() = values.size

    fun dot(other: Embedding): Float {
        require(dimension == other.dimension) { "Dimension mismatch: $dimension vs ${other.dimension}" }
        var sum = 0f
        for (i in values.indices) sum += values[i] * other.values[i]
        return sum
    }

    fun norm(): Float = sqrt(dot(this))

    fun cosine(other: Embedding): Float {
        val denom = norm() * other.norm()
        return if (denom == 0f) 0f else dot(other) / denom
    }

    fun normalized(): Embedding {
        val n = norm()
        if (n == 0f) return this
        return Embedding(FloatArray(dimension) { values[it] / n })
    }

    override fun equals(other: Any?) = other is Embedding && values.contentEquals(other.values)
    override fun hashCode() = values.contentHashCode()
    override fun toString() = "Embedding(dim=$dimension)"

    companion object {
        fun zeros(dimension: Int) = Embedding(FloatArray(dimension))
    }
}