package com.mruraza.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mruraza.data.entity.AgentSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAgentSession(agentSession: AgentSessionEntity)

    @Query("UPDATE agent_sessions SET status = :status WHERE sessionId = :sessionId")
    suspend fun changeAgentSessionStatus(sessionId: String,status: AgentSessionEntity.SessionStatus)

    @Query("SELECT * FROM agent_sessions")
    fun getSessions(): Flow<List<AgentSessionEntity>>

    @Query("SELECT * FROM agent_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): AgentSessionEntity?

    @Query("DELETE FROM agent_sessions WHERE sessionId = :sessionId")
    suspend fun deleteAgentSession(sessionId: String)
}