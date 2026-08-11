package com.mruraza.runtime_core.agent.executer

import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentResult
import com.mruraza.runtime_core.agent.core.AgentTask

class DefaultAgentExecuter: AgentExecuter {
    override suspend fun execute(
        agent: Agent,
        task: AgentTask
    ): AgentResult {
        return agent.execute(task)
    }
}