package com.mruraza.runtime_services.workflow

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryWorkflowRepo @Inject constructor() : WorkflowRepo {

    private val definitions = ConcurrentHashMap<String, WorkflowDefinition>()
    private val instances = ConcurrentHashMap<String, WorkflowInstance>()

    override suspend fun saveDefinition(definition: WorkflowDefinition) { definitions[definition.id] = definition }
    override suspend fun getDefinition(id: String) = definitions[id]
    override suspend fun listDefinitions() = definitions.values.sortedBy { it.name }
    override suspend fun deleteDefinition(id: String) { definitions.remove(id) }

    override suspend fun saveInstance(instance: WorkflowInstance) { instances[instance.id] = instance }
    override suspend fun getInstance(id: String) = instances[id]

    override suspend fun listInstances(definitionId: String?, status: WorkflowStatus?) =
        instances.values
            .filter { definitionId == null || it.definitionId == definitionId }
            .filter { status == null || it.status == status }
            .sortedByDescending { it.createdAt }

    override suspend fun deleteInstance(id: String) { instances.remove(id) }

    override suspend fun purgeFinished(olderThanMs: Long): Int {
        val cutoff = System.currentTimeMillis() - olderThanMs
        val old = instances.values.filter { it.status.isTerminal && (it.finishedAt ?: it.createdAt) < cutoff }
        old.forEach { instances.remove(it.id) }
        return old.size
    }

    override suspend fun clear() { definitions.clear(); instances.clear() }
}