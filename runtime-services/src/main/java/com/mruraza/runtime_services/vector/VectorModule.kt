package com.mruraza.runtime_services.vector

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class VectorModule {
    // Swap HashingEmbedder for a real model later
    @Binds abstract fun bindEmbedder(impl: HashingEmbedder): Embedder
    @Binds abstract fun bindStore(impl: InMemoryVectorStore): VectorStore
}