package com.mruraza.common.model

data class ContextPackage(
    val sessionId: String,
    val recentHistory: List<ConversationTurn>,
    val userPreferences: UserPreferences,
    val deviceContext: DeviceContext,
    val relevantMemories: List<MemoryEntry>,
    val activeWorkflows: WorkflowState?
)