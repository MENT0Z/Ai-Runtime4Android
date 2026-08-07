package com.mruraza.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mruraza.data.dao.AgentSessionDao
import com.mruraza.data.entity.AgentSessionEntity


@Database(
    entities = [AgentSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun agentSessionDao(): AgentSessionDao

    companion object {
        const val NAME = "agentruntime.db"
    }
}