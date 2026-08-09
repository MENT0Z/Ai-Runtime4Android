package com.mruraza.runtime_core.di

import com.mruraza.common.repo.AgentSessionRepo
import com.mruraza.common.repo.ConversationTurnRepo
import com.mruraza.common.repo.DeviceContextRepo
import com.mruraza.common.repo.UserPreferenceRepo
import com.mruraza.data.repo.AgentSessionRepoImpl
import com.mruraza.data.repo.ConversationRepoImpl
import com.mruraza.data.repo.DeviceContextRepoImpl
import com.mruraza.data.repo.UserPreferenceRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAgentSessionRepository(impl: AgentSessionRepoImpl): AgentSessionRepo

    @Binds
    @Singleton
    abstract fun bindConversationTurnRepo(impl: ConversationRepoImpl): ConversationTurnRepo

    @Binds
    @Singleton
    abstract fun bindDeviceContextRepo(impl: DeviceContextRepoImpl): DeviceContextRepo

    @Binds
    @Singleton
    abstract fun bidUserPrefRepo(impl: UserPreferenceRepoImpl): UserPreferenceRepo
}