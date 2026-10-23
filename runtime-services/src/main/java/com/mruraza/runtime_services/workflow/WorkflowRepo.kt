package com.mruraza.runtime_services.workflow

/** Storage for definitions and instances. In-memory now; implement with Room later. */
interface WorkflowRepo {
    suspend fun saveDefinition(definition: WorkflowDefinition)
    suspend fun getDefinition(id: String): WorkflowDefinition?
    suspend fun listDefinitions(): List<WorkflowDefinition>
    suspend fun deleteDefinition(id: String)

    suspend fun saveInstance(instance: WorkflowInstance)
    suspend fun getInstance(id: String): WorkflowInstance?
    suspend fun listInstances(
        definitionId: String? = null,
        status: WorkflowStatus? = null
    ): List<WorkflowInstance>
    suspend fun deleteInstance(id: String)
    /** Delete finished instances older than [olderThanMs]. Returns how many were removed. */
    suspend fun purgeFinished(olderThanMs: Long): Int
    suspend fun clear()
}