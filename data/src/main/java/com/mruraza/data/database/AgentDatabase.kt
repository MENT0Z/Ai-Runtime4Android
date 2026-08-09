package com.mruraza.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mruraza.data.dao.AgentSessionDao
import com.mruraza.data.dao.ConversationTurnDao
import com.mruraza.data.entity.AgentSessionEntity
import com.mruraza.data.entity.ConversationTurn


@Database(
    entities = [AgentSessionEntity::class, ConversationTurn::class],
    version = 1,
    exportSchema = false
)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun agentSessionDao(): AgentSessionDao
    abstract fun conversationTurnDao(): ConversationTurnDao

    companion object {
        const val NAME = "agentruntime.db"
    }
}