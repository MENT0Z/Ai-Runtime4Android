package com.mruraza.runtime_core.engine

import android.util.Log
import com.mruraza.common.model.ContextPackage
import com.mruraza.runtime_core.Context.ContextManager
import com.mruraza.runtime_core.Prompt.PromptManager
import com.mruraza.runtime_core.session.SessionManager
import javax.inject.Inject

class AgentRuntimeEngine @Inject constructor(
    private val sessionManager: SessionManager,
    private val contextManager: ContextManager,
    private val promptManager: PromptManager
) {

    suspend fun run() {
        val userMessage = "Hii There what is the full form of COMPUTER"

        // First, let's create the session.
        val session = sessionManager.startSession()

        // Now we have the session.
        val contextPackage =
            contextManager.refreshAndBuildContextWithDummyUserPref(
                session.sessionId
            )

        // Now we have the context package.
        val prompt = promptManager.compile(
            contextPackage,
            userMessage
        )

        Log.d("AgentRuntimeEngine", "Session ID: ${session.sessionId}")
        Log.d("AgentRuntimeEngine", "User Message: $userMessage")
        Log.d("AgentRuntimeEngine", "Context Package: $contextPackage")
        Log.d("AgentRuntimeEngine", "Compiled Prompt: $prompt")
    }
}