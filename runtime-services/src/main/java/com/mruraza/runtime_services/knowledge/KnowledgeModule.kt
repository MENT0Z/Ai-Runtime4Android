package com.mruraza.runtime_services.knowledge

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class KnowledgeModule {
    @Binds abstract fun bindGraph(impl: KnowledgeGraphRepo): KnowledgeGraph

    // Swap for a Room-backed implementation later
    @Binds abstract fun bindDataSource(impl: InMemoryKnowledgeGraphDataSource): KnowledgeGraphDataSource
}