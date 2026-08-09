package com.mruraza.data.repo

import com.mruraza.common.model.AgentSession
import com.mruraza.common.repo.AgentSessionRepo
import com.mruraza.data.dao.AgentSessionDao
import com.mruraza.data.entity.AgentSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AgentSessionRepoImpl @Inject constructor(
    private val agentSessionDao: AgentSessionDao
) : AgentSessionRepo {
    override suspend fun addAgentSession(agentSession: AgentSession) {
        agentSessionDao.addAgentSession(AgentSessionEntity.fromDomain(agentSession))
    }

    override suspend fun changeAgentSessionStatus(
        sessionId: String,
        status: AgentSession.SessionStatus
    ) {
        agentSessionDao.changeAgentSessionStatus(
            sessionId,
            AgentSessionEntity.SessionStatus.valueOf(status.name)
        )
    }

    override suspend fun deleteAgentSession(sessionId: String) {
        agentSessionDao.deleteAgentSession(sessionId)
    }

    override fun getSessions(): Flow<List<AgentSession>> {
        return agentSessionDao.getSessions().map { it: List<AgentSessionEntity> ->
            it.map { agentSession ->
                agentSession.toDomain()
            }
        }
    }

    override suspend fun getSessionById(sessionId: String): AgentSession? {
        return agentSessionDao.getSessionById(sessionId)?.toDomain()
    }

    override fun getActiveSessions(): Flow<List<AgentSession>> {
        return agentSessionDao.getActiveSessions().map {
            it.map { agentSession ->
                agentSession.toDomain()
            }
        }
    }
}