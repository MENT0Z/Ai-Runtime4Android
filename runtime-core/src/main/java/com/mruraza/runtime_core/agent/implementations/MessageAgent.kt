package com.mruraza.runtime_core.agent.implementations

import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentCapability
import com.mruraza.runtime_core.agent.core.AgentResult
import com.mruraza.runtime_core.agent.core.AgentTask

class MessageAgent : Agent{
    override val agentId: String = "message"

    override val name: String = "Messaging Agent"

    override val description: String = "Send and manage messages"

    override val capabilities: Set<AgentCapability> = setOf(
        AgentCapability(
            id = "message.send",
            description = "send message"
        ),
        AgentCapability(
            id = "message.delete",
            description = "delete message"
        )
    )

    override suspend fun execute(task: AgentTask): AgentResult {
        //TODO("Not yet implemented")
        return AgentResult.Success(
            taskId = task.taskId,
            output = "Ladies and GentleMan you got message from Ment0x",
            metadata = mapOf("platform" to "WhatsApp")
        )
    }

}