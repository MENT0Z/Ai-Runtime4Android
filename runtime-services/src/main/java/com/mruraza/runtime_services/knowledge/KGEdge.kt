package com.mruraza.runtime_services.knowledge

import java.util.UUID

/** Directed relation: fromId --relation--> toId. e.g. (Raza) --lives_in--> (Nepal) */
data class KGEdge(
    val id: String = UUID.randomUUID().toString(),
    val fromId: String,
    val toId: String,
    val relation: String,
    val weight: Float = 1f,
    val properties: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)
