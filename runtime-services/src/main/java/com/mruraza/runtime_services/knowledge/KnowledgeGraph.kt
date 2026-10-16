package com.mruraza.runtime_services.knowledge

enum class Direction { OUTGOING, INCOMING, BOTH }

data class KGPath(val nodes: List<KGNode>, val edges: List<KGEdge>)

data class KGSubgraph(val nodes: List<KGNode>, val edges: List<KGEdge>)

interface KnowledgeGraph {
    // ---- nodes ----
    suspend fun upsertNode(node: KGNode)
    suspend fun getNode(id: String): KGNode?
    suspend fun getNodeByLabel(label: String): KGNode?
    /** Removes the node AND all edges touching it. */
    suspend fun removeNode(id: String)
    /** Search by label/properties. Null/blank [query] = list by type. */
    suspend fun findNodes(query: String? = null, type: String? = null, limit: Int = 20): List<KGNode>
    /** Entity linking: nodes whose label appears as a word/phrase inside [text]. */
    suspend fun findMentioned(text: String, limit: Int = 10): List<KGNode>

    // ---- edges ----
    /** Same (from, relation, to) triple is updated instead of duplicated. Both nodes must exist. */
    suspend fun addEdge(edge: KGEdge): KGEdge
    suspend fun getEdge(id: String): KGEdge?
    suspend fun removeEdge(id: String)
    suspend fun getEdges(nodeId: String, direction: Direction = Direction.BOTH, relation: String? = null): List<KGEdge>

    // ---- traversal ----
    suspend fun neighbors(nodeId: String, direction: Direction = Direction.BOTH, relation: String? = null): List<KGNode>
    suspend fun shortestPath(fromId: String, toId: String, maxDepth: Int = 4, direction: Direction = Direction.BOTH): KGPath?
    suspend fun subgraph(centerId: String, depth: Int = 1, maxNodes: Int = 50): KGSubgraph

    // ---- misc ----
    suspend fun nodeCount(): Int
    suspend fun edgeCount(): Int
    suspend fun clear()
}