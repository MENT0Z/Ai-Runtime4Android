package com.mruraza.common.repo

import com.mruraza.common.model.AgentSession
import kotlinx.coroutines.flow.Flow

interface AgentSessionRepo{
        suspend fun addAgentSession(agentSession: AgentSession)
        suspend fun changeAgentSessionStatus(sessionId: String, status: AgentSession.SessionStatus)
        suspend fun deleteAgentSession(sessionId: String)
        fun getSessions(): Flow<List<AgentSession>>
        suspend fun getSessionById(sessionId: String): AgentSession?
        fun getActiveSessions(): Flow<List<AgentSession>>
}