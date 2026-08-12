package com.mruraza.runtime_core.agent.router

import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentTask
import com.mruraza.runtime_core.agent.registry.AgentRegistry
import javax.inject.Inject

class DefaultAgentRouter @Inject constructor(private val registry: AgentRegistry) : AgentRouter {
    override fun route(task: AgentTask): AgentRoutingDecision {
        val candidates: List<Agent> = registry.getByCapability(task.requiredCapability)
        val agent = candidates.firstOrNull() ?: throw IllegalStateException(
            "No agent available to execute task ${task.type} with taskId ${task.taskId}"
        )
        return AgentRoutingDecision(agent, 1.0, "Capability Match")
    }
}