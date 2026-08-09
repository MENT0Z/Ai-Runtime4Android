package com.mruraza.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mruraza.data.dao.AgentSessionDao
import com.mruraza.data.dao.ConversationTurnDao
import com.mruraza.data.dao.DeviceContextDao
import com.mruraza.data.dao.UserPreferencesDao
import com.mruraza.data.entity.AgentSessionEntity
import com.mruraza.data.entity.ConversationTurnEntity
import com.mruraza.data.entity.DeviceContextEntity
import com.mruraza.data.entity.UserPreferencesEntity


@Database(
    entities = [AgentSessionEntity::class, ConversationTurnEntity::class, UserPreferencesEntity::class, DeviceContextEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun agentSessionDao(): AgentSessionDao
    abstract fun conversationTurnDao(): ConversationTurnDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun deviceContextDao(): DeviceContextDao

    companion object {
        const val NAME = "agentruntime.db"
    }
}