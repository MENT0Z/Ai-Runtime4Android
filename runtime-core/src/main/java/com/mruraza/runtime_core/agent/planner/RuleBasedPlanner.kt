package com.mruraza.runtime_core.agent.planner

import com.mruraza.runtime_core.agent.core.AgentCapability
import com.mruraza.runtime_core.agent.core.AgentTask
import com.mruraza.runtime_core.agent.registry.AgentRegistry
import javax.inject.Inject

class RuleBasedPlanner @Inject constructor(private val registry: AgentRegistry) : Planner {
    override suspend fun plan(request: PlanningRequest): TaskGraph {
        //TODO("BUILD TASK GRAPH")
        return TaskGraph(
            rootTaskId = "message.send",
            tasks = mapOf("message.send" to DummyDataHolder.dummyMessageAgentTask)
        )
    }
}

// so for now let's create the dummy data as the part of AgentTask
object DummyDataHolder {
    val dummyMessageAgentTask = AgentTask(
        taskId = "message.send",
        type = "message",
        description = "send message",
        requiredCapability = AgentCapability(
            id = "message.send",
            description = "send message"
        ),
        input = mapOf(),
        dependencies = listOf()
    )
}
