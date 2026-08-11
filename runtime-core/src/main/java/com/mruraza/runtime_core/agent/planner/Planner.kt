package com.mruraza.runtime_core.agent.planner

import com.mruraza.common.model.ContextPackage

interface Planner{
    suspend fun plan(
        request: PlanningRequest
    ):TaskGraph
}

data class PlanningRequest(
    val requestId: String,
    val request: String,
    val context: ContextPackage
)