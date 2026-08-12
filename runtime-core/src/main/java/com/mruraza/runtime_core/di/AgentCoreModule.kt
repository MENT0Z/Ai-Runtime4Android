package com.mruraza.runtime_core.di

import com.mruraza.runtime_core.agent.executer.AgentExecuter
import com.mruraza.runtime_core.agent.executer.DefaultAgentExecuter
import com.mruraza.runtime_core.agent.executer.DefaultTaskGraphExecuter
import com.mruraza.runtime_core.agent.executer.TaskGraphExecuter
import com.mruraza.runtime_core.agent.planner.Planner
import com.mruraza.runtime_core.agent.planner.RuleBasedPlanner
import com.mruraza.runtime_core.agent.registry.AgentRegistry
import com.mruraza.runtime_core.agent.registry.DefaultAgentRegistry
import com.mruraza.runtime_core.agent.router.AgentRouter
import com.mruraza.runtime_core.agent.router.DefaultAgentRouter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AgentCoreModule {
    @Binds
    @Singleton
    abstract fun bindsAgentRegistry(impl: DefaultAgentRegistry): AgentRegistry

    @Binds
    @Singleton
    abstract fun bindsAgentRouter(impl: DefaultAgentRouter): AgentRouter

    @Binds
    @Singleton
    abstract fun bindsAgentPlanner(impl: RuleBasedPlanner): Planner

    @Binds
    @Singleton
    abstract fun bindsAgentExecuter(impl: DefaultAgentExecuter): AgentExecuter

    @Binds
    @Singleton
    abstract fun bindsTaskGraphExecuter(impl: DefaultTaskGraphExecuter): TaskGraphExecuter
}