package com.mruraza.runtime_core.agent.router

import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentTask

interface AgentRouter {
    fun route(
        task: AgentTask,
    ): AgentRoutingDecision
}

data class AgentRoutingDecision(
    val agent: Agent,
    val score: Double,
    val reason: String? = null
)