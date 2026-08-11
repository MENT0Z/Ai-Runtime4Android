package com.mruraza.runtime_core.agent.planner

import com.mruraza.runtime_core.agent.core.AgentTask

data class TaskGraph(
    val rootTaskId: String,
    val tasks: Map<String, AgentTask>
)