package com.mruraza.common.repo

import com.mruraza.common.model.ConversationTurn
import kotlinx.coroutines.flow.Flow

interface ConversationTurnRepo {
    fun getAllConversationTurnWithSessionId(sessionId: String): Flow<List<ConversationTurn>>
}