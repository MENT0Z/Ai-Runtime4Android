package com.mruraza.runtime_core.agent.registry

import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentCapability
import javax.inject.Inject

class DefaultAgentRegistry @Inject constructor() : AgentRegistry {

    private val agents = mutableMapOf<String, Agent>()

    override fun register(agent: Agent) {
        agents[agent.agentId] = agent
    }

    override fun unregister(agentId: String) {
        agents.remove(agentId)
    }

    override fun getAgent(agentId: String): Agent? {
        return agents[agentId]
    }

    override fun getByCapability(capability: AgentCapability): List<Agent> {
        return agents.values.filter { agent ->
            agent.capabilities.any { agentCapability ->
                agentCapability.id == capability.id
            }
        }
    }

    override fun getAllAgents(): List<Agent> {
        return agents.values.toList()
    }

    override fun contains(agentId: String): Boolean {
        return agents.containsKey(agentId)
    }

}