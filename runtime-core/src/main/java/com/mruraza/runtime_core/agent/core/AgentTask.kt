package com.mruraza.runtime_core.agent.core

data class AgentTask(
    val taskId: String,
    val type: String,
    val description: String,
    val requiredCapability: AgentCapability, // capability required to execute the task
    val input: Map<String, String> = emptyMap(), // input parameters for the task
    val dependencies: List<String> = emptyList() // list of task it depends
)