package com.mruraza.runtime_services.workflow

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkflowModule {
    // Swap for a Room-backed repo later
    @Binds abstract fun bindRepo(impl: InMemoryWorkflowRepo): WorkflowRepo
}