package com.mruraza.runtime_services.knowledge


import java.util.UUID

/** An entity in the graph: a person, place, app, concept... */
data class KGNode(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val type: String = "entity",
    val properties: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)