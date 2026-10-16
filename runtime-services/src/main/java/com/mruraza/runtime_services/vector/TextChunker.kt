package com.mruraza.runtime_services.vector

/** Splits long text into overlapping chunks (breaks at spaces when possible). */
object TextChunker {
    fun chunk(text: String, chunkSize: Int = 500, overlap: Int = 50): List<String> {
        require(chunkSize > 0) { "chunkSize must be > 0" }
        require(overlap in 0 until chunkSize) { "overlap must be in 0 until chunkSize" }

        val clean = text.trim()
        if (clean.isEmpty()) return emptyList()
        if (clean.length <= chunkSize) return listOf(clean)

        val out = mutableListOf<String>()
        var start = 0
        while (start < clean.length) {
            var end = minOf(start + chunkSize, clean.length)
            if (end < clean.length) {
                val lastSpace = clean.lastIndexOf(' ', end)
                if (lastSpace > start + chunkSize / 2) end = lastSpace
            }
            val piece = clean.substring(start, end).trim()
            if (piece.isNotEmpty()) out.add(piece)
            if (end >= clean.length) break
            start = maxOf(end - overlap, start + 1)
        }
        return out
    }
}