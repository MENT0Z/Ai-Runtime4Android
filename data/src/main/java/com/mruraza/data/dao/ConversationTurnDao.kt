package com.mruraza.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.mruraza.data.entity.ConversationTurnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationTurnDao {
    @Query("SELECT * FROM conversation_history WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getAllConversationsWithSessionId(sessionId: String): Flow<List<ConversationTurnEntity>>
}