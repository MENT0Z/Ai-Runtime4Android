package com.mruraza.runtime_core.agent.implementations

import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentCapability
import com.mruraza.runtime_core.agent.core.AgentResult
import com.mruraza.runtime_core.agent.core.AgentTask
import javax.inject.Inject

class ReminderAgent @Inject constructor() : Agent {
    override val agentId = "reminder"
    override val name = "Reminder Agent"
    override val description = "Creates and manages reminders."
    override val capabilities: Set<AgentCapability> = setOf(
        AgentCapability(
            id = "reminder.create",
            description = "Create a reminder"
        ),
        AgentCapability(
            id = "reminder.delete",
            description = "Delete a reminder"
        )
    )

    override suspend fun execute(task: AgentTask): AgentResult {
        // here we can pass the context too by creating the new dataclass that
        // embed both task with the contextPackage

        // todo the actual impl
        return AgentResult.Success(
            taskId = task.taskId,
            output = "Ladies and GentleMan you got reminder from Ment0x",
            metadata = mapOf("platform" to "Email")
        )
    }

}