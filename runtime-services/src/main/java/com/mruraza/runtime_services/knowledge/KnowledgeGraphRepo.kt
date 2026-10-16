package com.mruraza.runtime_services.knowledge

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Graph kept in RAM (adjacency maps) with write-through to [KnowledgeGraphDataSource].
 */
@Singleton
class KnowledgeGraphRepo @Inject constructor(
    private val dataSource: KnowledgeGraphDataSource
) : KnowledgeGraph {

    private val mutex = Mutex()
    private val nodes = LinkedHashMap<String, KGNode>()
    private val edges = LinkedHashMap<String, KGEdge>()
    private val outAdj = HashMap<String, MutableSet<String>>()
    private val inAdj = HashMap<String, MutableSet<String>>()
    private val tripleIndex = HashMap<String, String>() // triple key -> edgeId
    private var loaded = false

    // ---------------- internal (call only under lock) ----------------

    private suspend fun ensureLoaded() {
        if (loaded) return
        dataSource.loadNodes().forEach { nodes[it.id] = it }
        dataSource.loadEdges().forEach { if (it.fromId in nodes && it.toId in nodes) index(it) }
        loaded = true
    }

    private fun tripleKey(e: KGEdge) = "${e.fromId}|${e.relation.lowercase()}|${e.toId}"

    private fun index(e: KGEdge) {
        edges[e.id] = e
        outAdj.getOrPut(e.fromId) { mutableSetOf() }.add(e.id)
        inAdj.getOrPut(e.toId) { mutableSetOf() }.add(e.id)
        tripleIndex[tripleKey(e)] = e.id
    }

    private fun unindex(e: KGEdge) {
        edges.remove(e.id)
        outAdj[e.fromId]?.remove(e.id)
        inAdj[e.toId]?.remove(e.id)
        tripleIndex.remove(tripleKey(e))
    }

    private fun incident(nodeId: String, direction: Direction, relation: String?): List<KGEdge> {
        val ids: Set<String> = when (direction) {
            Direction.OUTGOING -> outAdj[nodeId].orEmpty()
            Direction.INCOMING -> inAdj[nodeId].orEmpty()
            Direction.BOTH -> outAdj[nodeId].orEmpty() + inAdj[nodeId].orEmpty()
        }
        return ids.mapNotNull { edges[it] }
            .filter { relation == null || it.relation.equals(relation, ignoreCase = true) }
    }

    private fun otherEnd(e: KGEdge, nodeId: String) = if (e.fromId == nodeId) e.toId else e.fromId

    private fun scoreNode(node: KGNode, q: String?): Int {
        if (q.isNullOrEmpty()) return 1
        val label = node.label.lowercase()
        return when {
            label == q -> 3
            label.contains(q) -> 2
            node.properties.any { (k, v) -> k.lowercase().contains(q) || v.lowercase().contains(q) } -> 1
            else -> 0
        }
    }

    private fun containsWord(haystack: String, needle: String): Boolean {
        var from = 0
        while (true) {
            val i = haystack.indexOf(needle, from)
            if (i < 0) return false
            val before = if (i == 0) ' ' else haystack[i - 1]
            val after = if (i + needle.length >= haystack.length) ' ' else haystack[i + needle.length]
            if (!before.isLetterOrDigit() && !after.isLetterOrDigit()) return true
            from = i + 1
        }
    }

    // ---------------- nodes ----------------

    override suspend fun upsertNode(node: KGNode) = mutex.withLock {
        ensureLoaded()
        nodes[node.id] = node
        dataSource.saveNode(node)
    }

    override suspend fun getNode(id: String): KGNode? = mutex.withLock {
        ensureLoaded(); nodes[id]
    }

    override suspend fun getNodeByLabel(label: String): KGNode? = mutex.withLock {
        ensureLoaded()
        nodes.values.firstOrNull { it.label.equals(label.trim(), ignoreCase = true) }
    }

    override suspend fun removeNode(id: String) = mutex.withLock {
        ensureLoaded()
        if (id !in nodes) return@withLock
        val touching = (outAdj[id].orEmpty() + inAdj[id].orEmpty()).toList()
        for (eid in touching) {
            edges[eid]?.let { unindex(it) }
            dataSource.deleteEdge(eid)
        }
        outAdj.remove(id); inAdj.remove(id)
        nodes.remove(id)
        dataSource.deleteNode(id)
    }

    override suspend fun findNodes(query: String?, type: String?, limit: Int): List<KGNode> = mutex.withLock {
        ensureLoaded()
        val q = query?.trim()?.lowercase()
        nodes.values
            .filter { type == null || it.type.equals(type, ignoreCase = true) }
            .map { it to scoreNode(it, q) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit.coerceAtLeast(0))
            .map { it.first }
    }

    override suspend fun findMentioned(text: String, limit: Int): List<KGNode> = mutex.withLock {
        ensureLoaded()
        val t = text.lowercase()
        nodes.values
            .filter { it.label.length >= 2 && containsWord(t, it.label.lowercase()) }
            .sortedByDescending { it.label.length } // prefer longer / more specific names
            .take(limit.coerceAtLeast(0))
    }

    // ---------------- edges ----------------

    override suspend fun addEdge(edge: KGEdge): KGEdge = mutex.withLock {
        ensureLoaded()
        require(edge.fromId in nodes) { "Unknown fromId: ${edge.fromId}" }
        require(edge.toId in nodes) { "Unknown toId: ${edge.toId}" }
        require(edge.relation.isNotBlank()) { "Relation must not be blank" }
        val existingId = tripleIndex[tripleKey(edge)]
        val stored = if (existingId != null) edge.copy(id = existingId) else edge
        index(stored)
        dataSource.saveEdge(stored)
        stored
    }

    override suspend fun getEdge(id: String): KGEdge? = mutex.withLock {
        ensureLoaded(); edges[id]
    }

    override suspend fun removeEdge(id: String) = mutex.withLock {
        ensureLoaded()
        edges[id]?.let { unindex(it); dataSource.deleteEdge(id) }
        Unit
    }

    override suspend fun getEdges(nodeId: String, direction: Direction, relation: String?): List<KGEdge> =
        mutex.withLock {
            ensureLoaded(); incident(nodeId, direction, relation)
        }

    // ---------------- traversal ----------------

    override suspend fun neighbors(nodeId: String, direction: Direction, relation: String?): List<KGNode> =
        mutex.withLock {
            ensureLoaded()
            incident(nodeId, direction, relation)
                .mapNotNull { nodes[otherEnd(it, nodeId)] }
                .distinctBy { it.id }
        }

    override suspend fun shortestPath(
        fromId: String, toId: String, maxDepth: Int, direction: Direction
    ): KGPath? = mutex.withLock {
        ensureLoaded()
        if (fromId !in nodes || toId !in nodes) return@withLock null
        if (fromId == toId) return@withLock KGPath(listOf(nodes.getValue(fromId)), emptyList())

        val parent = HashMap<String, Pair<String, KGEdge>>() // node -> (previous node, edge used)
        val visited = hashSetOf(fromId)
        var frontier = listOf(fromId)
        var depth = 0
        var found = false

        while (frontier.isNotEmpty() && depth < maxDepth && !found) {
            val next = mutableListOf<String>()
            for (cur in frontier) {
                for (e in incident(cur, direction, null)) {
                    val other = otherEnd(e, cur)
                    if (visited.add(other)) {
                        parent[other] = cur to e
                        if (other == toId) { found = true; break }
                        next.add(other)
                    }
                }
                if (found) break
            }
            frontier = next
            depth++
        }
        if (!found) return@withLock null

        val pathNodes = ArrayList<KGNode>()
        val pathEdges = ArrayList<KGEdge>()
        var cur = toId
        while (cur != fromId) {
            val (prev, edge) = parent.getValue(cur)
            pathNodes.add(nodes.getValue(cur))
            pathEdges.add(edge)
            cur = prev
        }
        pathNodes.add(nodes.getValue(fromId))
        KGPath(pathNodes.reversed(), pathEdges.reversed())
    }

    override suspend fun subgraph(centerId: String, depth: Int, maxNodes: Int): KGSubgraph = mutex.withLock {
        ensureLoaded()
        if (centerId !in nodes) return@withLock KGSubgraph(emptyList(), emptyList())

        val visited = LinkedHashSet<String>().apply { add(centerId) }
        var frontier = listOf(centerId)
        var d = 0
        while (frontier.isNotEmpty() && d < depth && visited.size < maxNodes) {
            val next = mutableListOf<String>()
            for (cur in frontier) {
                for (e in incident(cur, Direction.BOTH, null)) {
                    val other = otherEnd(e, cur)
                    if (visited.size >= maxNodes) break
                    if (visited.add(other)) next.add(other)
                }
            }
            frontier = next
            d++
        }
        val subEdges = visited
            .flatMap { incident(it, Direction.OUTGOING, null) }
            .filter { it.toId in visited }
        KGSubgraph(visited.mapNotNull { nodes[it] }, subEdges)
    }

    // ---------------- misc ----------------

    override suspend fun nodeCount(): Int = mutex.withLock { ensureLoaded(); nodes.size }
    override suspend fun edgeCount(): Int = mutex.withLock { ensureLoaded(); edges.size }

    override suspend fun clear() = mutex.withLock {
        nodes.clear(); edges.clear(); outAdj.clear(); inAdj.clear(); tripleIndex.clear()
        dataSource.clear()
        loaded = true
    }
}