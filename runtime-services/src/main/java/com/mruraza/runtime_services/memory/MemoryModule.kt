package com.mruraza.runtime_services.memory

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MemoryModule {

    @Binds abstract fun bindShortTerm(impl: InMemoryShortTermMemory): ShortTermMemory

    @Binds abstract fun bindLongTerm(impl: MemoryRepo): LongTermMemory

    // Swap to your Room-backed implementation later:
    @Binds abstract fun bindDataSource(impl: InMemoryMemoryDataSource): MemoryDataSource
}