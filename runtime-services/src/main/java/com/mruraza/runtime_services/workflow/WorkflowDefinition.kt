package com.mruraza.runtime_services.workflow

/** A reusable blueprint: a DAG of steps. */
data class WorkflowDefinition(
    val id: String,
    val name: String,
    val description: String = "",
    val steps: List<WorkflowStep>,
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Empty list = valid. */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (steps.isEmpty()) errors += "Workflow has no steps"

        val ids = steps.map { it.id }
        ids.groupBy { it }.filter { it.value.size > 1 }.keys.forEach { errors += "Duplicate step id: $it" }
        steps.filter { it.id.isBlank() }.forEach { errors += "Step with blank id (name=${it.name})" }

        val idSet = ids.toSet()
        for (s in steps) {
            for (d in s.dependsOn) {
                if (d == s.id) errors += "Step ${s.id} depends on itself"
                else if (d !in idSet) errors += "Step ${s.id} depends on unknown step $d"
            }
            if (s.maxRetries < 0) errors += "Step ${s.id}: maxRetries < 0"
        }
        if (errors.isEmpty() && topologicalOrder() == null) errors += "Workflow has a dependency cycle"
        return errors
    }

    /** Steps in a valid execution order, or null if there is a cycle / unknown dependency. */
    fun topologicalOrder(): List<WorkflowStep>? {
        val byId = steps.associateBy { it.id }
        val inDegree = HashMap<String, Int>()
        val dependents = HashMap<String, MutableList<String>>()
        for (s in steps) {
            inDegree[s.id] = s.dependsOn.size
            for (d in s.dependsOn) {
                if (d !in byId) return null
                dependents.getOrPut(d) { mutableListOf() }.add(s.id)
            }
        }
        val queue = ArrayDeque(steps.filter { inDegree.getValue(it.id) == 0 }.map { it.id })
        val order = mutableListOf<WorkflowStep>()
        while (queue.isNotEmpty()) {
            val id = queue.removeFirst()
            order += byId.getValue(id)
            for (dep in dependents[id].orEmpty()) {
                val left = inDegree.getValue(dep) - 1
                inDegree[dep] = left
                if (left == 0) queue.addLast(dep)
            }
        }
        return if (order.size == steps.size) order else null
    }

    fun step(id: String): WorkflowStep? = steps.firstOrNull { it.id == id }
}

