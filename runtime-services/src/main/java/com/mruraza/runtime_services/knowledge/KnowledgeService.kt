package com.mruraza.runtime_services.knowledge

import javax.inject.Inject
import javax.inject.Singleton

/** Friendly API for the ContextManager / agents. Works with labels instead of ids. */
@Singleton
class KnowledgeService @Inject constructor(
    private val graph: KnowledgeGraph
) {

    /** Find node by label or create it. */
    suspend fun ensureNode(
        label: String,
        type: String = "entity",
        properties: Map<String, String> = emptyMap()
    ): KGNode {
        graph.getNodeByLabel(label)?.let { existing ->
            if (properties.isEmpty()) return existing
            val merged = existing.copy(properties = existing.properties + properties)
            graph.upsertNode(merged)
            return merged
        }
        val node = KGNode(label = label.trim(), type = type, properties = properties)
        graph.upsertNode(node)
        return node
    }

    /** "Raza" --lives_in--> "Nepal". Creates missing nodes automatically. */
    suspend fun relate(
        subject: String,
        relation: String,
        obj: String,
        subjectType: String = "entity",
        objectType: String = "entity",
        weight: Float = 1f
    ): KGEdge {
        val s = ensureNode(subject, subjectType)
        val o = ensureNode(obj, objectType)
        return graph.addEdge(
            KGEdge(fromId = s.id, toId = o.id, relation = relation.trim(), weight = weight)
        )
    }

    /** Human-readable facts about every known entity mentioned in [text]. */
    suspend fun factsAbout(text: String, depth: Int = 1, maxFacts: Int = 15): List<String> {
        val mentioned = graph.findMentioned(text, limit = 5)
        if (mentioned.isEmpty()) return emptyList()
        val facts = LinkedHashMap<String, Float>()
        for (node in mentioned) {
            val sub = graph.subgraph(node.id, depth, maxNodes = 30)
            val byId = sub.nodes.associateBy { it.id }
            for (e in sub.edges) {
                val from = byId[e.fromId] ?: continue
                val to = byId[e.toId] ?: continue
                facts["${from.label} ${e.relation.replace('_', ' ')} ${to.label}"] = e.weight
            }
        }
        return facts.entries.sortedByDescending { it.value }.take(maxFacts).map { it.key }
    }

    /** Prompt block ready for the system prompt; empty string if nothing known. */
    suspend fun buildPrompt(text: String, depth: Int = 1, maxFacts: Int = 15): String {
        val facts = factsAbout(text, depth, maxFacts)
        if (facts.isEmpty()) return ""
        return "Known facts:\n" + facts.joinToString("\n") { "- $it" }
    }

    /** "A -> rel -> B -> rel -> C" or null if not connected. */
    suspend fun explainConnection(a: String, b: String, maxDepth: Int = 4): String? {
        val from = graph.getNodeByLabel(a) ?: return null
        val to = graph.getNodeByLabel(b) ?: return null
        val path = graph.shortestPath(from.id, to.id, maxDepth) ?: return null
        val sb = StringBuilder(path.nodes.first().label)
        path.edges.forEachIndexed { i, e ->
            sb.append(" -[${e.relation}]- ").append(path.nodes[i + 1].label)
        }
        return sb.toString()
    }

    suspend fun forget(label: String) {
        graph.getNodeByLabel(label)?.let { graph.removeNode(it.id) }
    }

    suspend fun search(query: String, limit: Int = 10) = graph.findNodes(query, null, limit)
}