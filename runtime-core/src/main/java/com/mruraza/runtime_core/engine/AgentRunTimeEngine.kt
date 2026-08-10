package com.mruraza.runtime_core.engine

import com.mruraza.runtime_core.Context.ContextManager
import com.mruraza.runtime_core.Prompt.PromptManager
import com.mruraza.runtime_core.session.SessionManager
import javax.inject.Inject

class AgentRuntimeEngine @Inject constructor(
    private val sessionManager: SessionManager,
    private val contextManager: ContextManager,
    private val promptManager: PromptManager
){

}