package com.mruraza.runtime_core.engine

import android.util.Log
import com.mruraza.common.model.ContextPackage
import com.mruraza.runtime_core.Context.ContextManager
import com.mruraza.runtime_core.Prompt.PromptManager
import com.mruraza.runtime_core.agent.core.Agent
import com.mruraza.runtime_core.agent.core.AgentResult
import com.mruraza.runtime_core.agent.core.AgentTask
import com.mruraza.runtime_core.agent.executer.AgentExecuter
import com.mruraza.runtime_core.agent.executer.TaskGraphExecuter
import com.mruraza.runtime_core.agent.implementations.CalendarAgent
import com.mruraza.runtime_core.agent.implementations.MessageAgent
import com.mruraza.runtime_core.agent.implementations.ReminderAgent
import com.mruraza.runtime_core.agent.planner.Planner
import com.mruraza.runtime_core.agent.planner.PlanningRequest
import com.mruraza.runtime_core.agent.registry.AgentRegistry
import com.mruraza.runtime_core.agent.router.AgentRouter
import com.mruraza.runtime_core.session.SessionManager
import java.util.UUID
import javax.inject.Inject

class AgentRuntimeEngine @Inject constructor(
    private val sessionManager: SessionManager,
    private val contextManager: ContextManager,
    private val promptManager: PromptManager,
    private val agentRegistry: AgentRegistry,
    private val agentPlanner: Planner,
    private val agentRouter: AgentRouter,
    private val agentExecuter: AgentExecuter,
    private val taskGraphExecuter: TaskGraphExecuter
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

//        // Now we have the context package.
//        val prompt = promptManager.compile(
//            contextPackage,
//            userMessage
//        )
//
//        Log.d("AgentRuntimeEngine", "Session ID: ${session.sessionId}")
//        Log.d("AgentRuntimeEngine", "User Message: $userMessage")
//        Log.d("AgentRuntimeEngine", "Context Package: $contextPackage")
//        Log.d("AgentRuntimeEngine", "Compiled Prompt: $prompt")

        // so now lets implement the rest of the workflow

        // step1 Register the agents
        GetAllAgentsToRegister.getAllAgentsToRegister().forEach {
            agentRegistry.register(it)
        }

        // step2 plan the task graph
        val taskGraph = agentPlanner.plan(request = PlanningRequest(
            requestId = UUID.randomUUID().toString(),
            request = userMessage,
            context = contextPackage
        ))

        // step 3
        // if the task graph has single task then executeTask else executeGraph
        val taskGraphSize: Int = taskGraph.tasks.size

        val executionRes: AgentResult = if(taskGraphSize==1){
            val task: AgentTask = taskGraph.tasks.values.first()
            agentExecuter.execute(agent = (agentRouter.route(task).agent),task=task)
        }else{
            // todo need to fix this logic to get all res and work in that parallely or sequentially
            taskGraphExecuter.execute(graph = taskGraph).taskResult.values.first()
        }

        // step 4 lets verify by all logs
        Log.d("AgentRuntimeEngine", "Execution Result: $executionRes")
    }
}

object GetAllAgentsToRegister{
    fun getAllAgentsToRegister(): List<Agent>{
        return listOf(
            MessageAgent(),
            CalendarAgent(),
            ReminderAgent()
        )
    }
}