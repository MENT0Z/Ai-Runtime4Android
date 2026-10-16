package com.mruraza.runtime_services.knowledge


import javax.inject.Inject
import javax.inject.Singleton

// TODO need to implement later with Room
/** Persistence seam. Implement with Room later and change the binding in KnowledgeModule. */
interface KnowledgeGraphDataSource {
    suspend fun saveNode(node: KGNode)
    suspend fun deleteNode(id: String)
    suspend fun saveEdge(edge: KGEdge)
    suspend fun deleteEdge(id: String)
    suspend fun loadNodes(): List<KGNode>
    suspend fun loadEdges(): List<KGEdge>
    suspend fun clear()
}

@Singleton
class InMemoryKnowledgeGraphDataSource @Inject constructor() : KnowledgeGraphDataSource {
    private val nodes = HashMap<String, KGNode>()
    private val edges = HashMap<String, KGEdge>()

    override suspend fun saveNode(node: KGNode) { synchronized(this) { nodes[node.id] = node } }
    override suspend fun deleteNode(id: String) { synchronized(this) { nodes.remove(id) } }
    override suspend fun saveEdge(edge: KGEdge) { synchronized(this) { edges[edge.id] = edge } }
    override suspend fun deleteEdge(id: String) { synchronized(this) { edges.remove(id) } }
    override suspend fun loadNodes() = synchronized(this) { nodes.values.toList() }
    override suspend fun loadEdges() = synchronized(this) { edges.values.toList() }
    override suspend fun clear() { synchronized(this) { nodes.clear(); edges.clear() } }
}