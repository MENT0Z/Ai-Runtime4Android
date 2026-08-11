package com.mruraza.runtime_core.agent.registry

import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentCapability

interface AgentRegistry{
    fun register(agent: Agent)
    fun unregister(agentId: String)
    fun getAgent(agentId: String): Agent?
    fun getByCapability(capability: AgentCapability):List<Agent>
    fun getAllAgents():List<Agent>
    fun contains(agentId: String):Boolean
}