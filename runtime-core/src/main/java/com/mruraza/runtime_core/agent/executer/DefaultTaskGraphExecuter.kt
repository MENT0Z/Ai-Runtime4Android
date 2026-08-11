package com.mruraza.runtime_core.agent.executer

import com.mruraza.runtime_core.agent.planner.TaskGraph
import com.mruraza.runtime_core.agent.router.AgentRouter

class DefaultTaskGraphExecuter(
    private val executer: AgentExecuter,
    private val router: AgentRouter
) : TaskGraphExecuter {
    override suspend fun execute(graph: TaskGraph): TaskGraphResult {
        // todo
        // so here in the agentTask if we get the dependencies between the task then process
        // according to the dependency else we can process them parallely
        val res = TaskGraphResult(
            graph = graph,
            taskResult = emptyMap()
        )
        return res
    }
}