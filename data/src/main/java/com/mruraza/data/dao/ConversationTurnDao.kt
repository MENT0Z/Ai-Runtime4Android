package com.mruraza.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.mruraza.data.entity.ConversationTurn

@Dao
interface ConversationTurnDao {
    @Query("SELECT * FROM conversation_history WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getAllConversationsWithSessionId(sessionId: String): List<ConversationTurn>
}