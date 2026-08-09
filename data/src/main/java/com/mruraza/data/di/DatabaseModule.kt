package com.mruraza.data.di

import android.content.Context
import androidx.room.Room
import com.mruraza.data.dao.AgentSessionDao
import com.mruraza.data.dao.ConversationTurnDao
import com.mruraza.data.dao.DeviceContextDao
import com.mruraza.data.dao.UserPreferencesDao
import com.mruraza.data.database.AgentDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule{
    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context
    ): AgentDatabase{
        return Room.databaseBuilder(
                context = context,
                klass = AgentDatabase::class.java,
                name = AgentDatabase.NAME
            ).fallbackToDestructiveMigration(false).build()
    }

    @Provides
    fun provideConversationTurnDao(db: AgentDatabase): ConversationTurnDao = db.conversationTurnDao()

    @Provides
    fun provideAgentSessionDao(db: AgentDatabase): AgentSessionDao = db.agentSessionDao()

    @Provides
    fun provideUserPreferencesDao(db: AgentDatabase): UserPreferencesDao = db.userPreferencesDao()

    @Provides
    fun provideDeviceContextDao(db: AgentDatabase): DeviceContextDao = db.deviceContextDao()
}