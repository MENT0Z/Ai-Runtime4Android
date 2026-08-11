package com.mruraza.runtime_core.agent.core

interface Agent{
    val agentId: String
    val name: String
    val description: String
    val capabilities: Set<AgentCapabilities>
    suspend fun execute(task: AgentTask):AgentResult
}