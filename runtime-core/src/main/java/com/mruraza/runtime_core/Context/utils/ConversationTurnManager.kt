package com.mruraza.runtime_core.Context.utils

import com.mruraza.common.model.ConversationTurn
import com.mruraza.common.repo.ConversationTurnRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Coordinates conversation-turn operations.
 *
 * The manager is responsible for conversation-level orchestration,
 * while persistence is delegated to [ConversationTurnRepo].
 *
 * Future LLM integration can be added here, for example:
 *
 * 1. Persist the user's message.
 * 2. Build the LLM request from the conversation history.
 * 3. Invoke the LLM.
 * 4. Persist the assistant response.
 */
class ConversationTurnManager @Inject constructor(
    private val conversationTurnRepo: ConversationTurnRepo
) {

    /**
     * Observes all conversation turns belonging to a session.
     *
     * The returned [Flow] emits whenever the underlying Room data changes.
     */
    fun observeConversation(sessionId: String): Flow<List<ConversationTurn>> {
        return conversationTurnRepo.getAllConversationTurnWithSessionId(sessionId)
    }

    /**
     * Persists a single conversation turn.
     *
     * This is useful for turns that are already fully constructed,
     * such as SYSTEM, TOOL, or ASSISTANT messages.
     */
    suspend fun addTurn(turn: ConversationTurn) {
        conversationTurnRepo.addConversation(turn)
    }

    /**
     * Adds a user message to the conversation.
     *
     * This is intentionally kept separate from the actual LLM interaction.
     * When LLM integration is added, this method can become the entry point
     * for the complete user -> LLM -> assistant flow.
     */
    suspend fun sendUserMessage(
        sessionId: String,
        content: String
    ) {
        require(content.isNotBlank()) {
            "Conversation message content must not be blank."
        }

        val userTurn = ConversationTurn(
            sessionId = sessionId,
            role = ConversationTurn.ConversationRole.USER,
            content = content
        )

        conversationTurnRepo.addConversation(userTurn)


        // 1. Load conversation history.
        // 2. Send the conversation to the LLM.
        // 3. Handle tool calls if required.
        // 4. Persist the assistant response.
    }
}