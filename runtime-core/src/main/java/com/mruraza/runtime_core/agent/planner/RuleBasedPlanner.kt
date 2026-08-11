package com.mruraza.runtime_core.agent.planner

import com.mruraza.runtime_core.agent.registry.AgentRegistry

class RuleBasedPlanner (private val registry: AgentRegistry): Planner{
    override suspend fun plan(request: PlanningRequest): TaskGraph {
        //TODO("BUILD TASK GRAPH")
        return TaskGraph(
            rootTaskId = "root",
            tasks = emptyMap()
        )
    }
}