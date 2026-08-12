package com.mruraza.runtime_core.agent.implementations

import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentCapability
import com.mruraza.runtime_core.agent.core.AgentResult
import com.mruraza.runtime_core.agent.core.AgentTask
import javax.inject.Inject

class CalendarAgent @Inject constructor() : Agent {

    override val agentId = "calendar"

    override val name = "Calendar Agent"

    override val description =
        "Creates, updates and manages calendar events."

    override val capabilities = setOf(
        AgentCapability(
            id = "calendar.create",
            description = "Create calendar events"
        ),
        AgentCapability(
            id = "calendar.update",
            description = "Update calendar events"
        ),
        AgentCapability(
            id = "calendar.delete",
            description = "Delete calendar events"
        )
    )

    override suspend fun execute(
        task: AgentTask
    ): AgentResult {
        // TODO()
        // basically for each of the capabilites we need to have the separate way to handle execution
        return AgentResult.Success(
            taskId = task.taskId,
            output = "Ladies and GentleMan your calender is marked for Ment0x bday",
            metadata = mapOf("platform" to "Google Calender")
        )
    }
}