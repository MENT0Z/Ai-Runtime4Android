package com.mruraza.runtime_core.agent.core

data class AgentTask(
    val taskId: String,
    val type: String,
    val description: String,
    val requiredCapability: AgentCapability,
    val input: Map<String, String> = emptyMap(),
    val dependencies: List<String> = emptyList()
)