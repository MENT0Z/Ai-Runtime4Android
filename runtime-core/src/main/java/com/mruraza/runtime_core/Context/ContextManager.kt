package com.mruraza.runtime_core.Context

// here we have to write the
/*
* data class ContextPackage(
    val sessionId: String,
    val recentHistory: List<ConversationTurn>,
    val userPreferences: UserPreferences,
    val deviceContext: DeviceContext,
    val relevantMemories: List<MemoryEntry>,
    val activeWorkflows: WorkflowState?
)
* */


import android.util.Log
import com.mruraza.common.model.ContextPackage
import com.mruraza.common.model.UserPreferences
import com.mruraza.common.model.WorkflowState
import com.mruraza.runtime_core.Context.utils.ConversationTurnManager
import com.mruraza.runtime_core.Context.utils.DeviceContextManager
import com.mruraza.runtime_core.Context.utils.UserPreferenceManager
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Coordinates all context-related managers and assembles the context required
 * by the LLM for a given conversation session.
 *
 * ContextManager acts as the orchestration layer between:
 *
 * - ConversationTurnManager  -> recent conversation history
 * - UserPreferenceManager    -> persisted user preferences
 * - DeviceContextManager     -> current device/application context
 * - Memory system             -> relevant long-term memories (future)
 * - Workflow system           -> active workflows (future)
 *
 * The manager intentionally does not contain persistence or collection logic
 * itself. Each responsibility remains delegated to its corresponding manager.
 *
 * Memory and workflow support are currently placeholders and will be
 * implemented in a future application version.
 */
class ContextManager @Inject constructor(
    private val conversationTurnManager: ConversationTurnManager,
    private val userPreferenceManager: UserPreferenceManager,
    private val deviceContextManager: DeviceContextManager,
) {

    /**
     * Builds the complete context package for a conversation session.
     *
     * The context package contains:
     *
     * 1. Session identifier
     * 2. Recent conversation history
     * 3. User preferences
     * 4. Current device context
     * 5. Relevant long-term memories
     * 6. Active workflow state
     *
     * Memory retrieval and workflow state retrieval are intentionally left
     * empty until those systems are implemented.
     *
     * @param sessionId ID of the conversation session.
     *
     * @return [ContextPackage] containing all currently available context.
     *
     * @throws IllegalArgumentException if [sessionId] is blank.
     */
    suspend fun buildContext(sessionId: String): ContextPackage {
        require(sessionId.isNotBlank()) {
            "Session ID must not be blank."
        }

        // TODO here need to call this from the shortTermMemo from run-time-services
        val recentHistory = conversationTurnManager
            .observeConversation(sessionId)
            .first()

        val userPreferences = userPreferenceManager
            .getUserPreferences()

        val deviceContext = deviceContextManager
            .getDeviceContext()

        /*
         * TODO: Implement memory retrieval in the next application version.
         *
         * Expected responsibility:
         * - Retrieve memories relevant to the current session/query.
         * - Perform semantic/vector similarity search where applicable.
         * - Apply relevance filtering/ranking.
         * - Return only memories useful to the current LLM request.
         */

        /*
         * TODO: Implement workflow state retrieval in the next application
         * version.
         *
         * Expected responsibility:
         * - Determine whether the session currently has an active workflow.
         * - Retrieve the current workflow state.
         * - Provide the workflow state to the LLM/tool orchestration layer.
         */
        val activeWorkflows: WorkflowState? = null

        return ContextPackage(
            sessionId = sessionId,
            recentHistory = recentHistory,
            userPreferences = userPreferences,
            deviceContext = deviceContext,
            relevantMemories = listOf(),
            activeWorkflows = activeWorkflows,
        )
    }

    /**
     * Builds context using the latest persisted device context.
     *
     * This method is useful when the application has already initialized
     * and persisted the current device context.
     *
     * This is currently an alias for [buildContext] and exists as a semantic
     * entry point for future context-building strategies.
     */
    suspend fun getContext(sessionId: String): ContextPackage {
        return buildContext(sessionId)
    }

    /**
     * Collects the latest device context and persists it before building
     * the context package.
     *
     * This can be used when the application needs to ensure that the
     * ContextPackage contains freshly collected device information.
     *
     * @param sessionId ID of the conversation session.
     *
     * @return [ContextPackage] containing the freshly collected device context.
     */
    suspend fun refreshAndBuildContext(
        sessionId: String,
    ): ContextPackage {
        require(sessionId.isNotBlank()) {
            "Session ID must not be blank."
        }

        deviceContextManager.collectAndSaveDeviceContext()

        return buildContext(sessionId)
    }

    suspend fun refreshAndBuildContextWithDummyUserPref(
        sessionId: String
    ): ContextPackage{
        require(sessionId.isNotBlank()) {
            "Session ID must not be blank."
        }
        val dummyUserPref = UserPreferences(
            language = "en",
            timezone = "America/New_York",
            preferredTone = "formal",
            responseLength = UserPreferences.ResponseLength.SHORT,
            preferences = mapOf(
                "formatted style" to "gen-z style"
            )
        )
        userPreferenceManager.saveUserPreferences(dummyUserPref)
        Log.d("ContextManager", "Dummy user preferences saved.")
        deviceContextManager.collectAndSaveDeviceContext()
        Log.d("ContextManager", "Device context saved.")
        return getContext(sessionId)
    }

}



