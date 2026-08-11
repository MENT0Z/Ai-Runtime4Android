package com.mruraza.runtime_core.agent.executer

import com.mruraza.runtime_core.agent.core.AgentResult
import com.mruraza.runtime_core.agent.planner.TaskGraph

interface TaskGraphExecuter{
    suspend fun execute(graph: TaskGraph):TaskGraphResult
}

data class TaskGraphResult(
    val graph: TaskGraph,
    val taskResult : Map<String, AgentResult>
)