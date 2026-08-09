package com.mruraza.data.repo

import com.mruraza.common.model.ConversationTurn
import com.mruraza.common.repo.ConversationTurnRepo
import com.mruraza.data.dao.ConversationTurnDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConversationRepoImpl @Inject constructor(
    private val conversationTurnDao: ConversationTurnDao
) : ConversationTurnRepo {
    override fun getAllConversationTurnWithSessionId(sessionId: String): Flow<List<ConversationTurn>> {
        return conversationTurnDao.getAllConversationsWithSessionId(sessionId).map {
            it.map { conversationTurnEntity ->
                ConversationTurn(
                    conversationId = conversationTurnEntity.conversationId,
                    sessionId = conversationTurnEntity.sessionId,
                    createdAt = conversationTurnEntity.createdAt,
                    role = ConversationTurn.ConversationRole.valueOf(conversationTurnEntity.role.name),
                    content = conversationTurnEntity.content
                )
            }
        }
    }

}