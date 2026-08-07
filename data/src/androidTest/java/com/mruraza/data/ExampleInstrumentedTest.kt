package com.mruraza.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mruraza.data.dao.AgentSessionDao
import com.mruraza.data.database.AgentDatabase
import com.mruraza.data.entity.AgentSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.mruraza.data.test", appContext.packageName)
    }

    private lateinit var agentDatabase: AgentDatabase
    private lateinit var agentSessionDao: AgentSessionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        agentDatabase = Room.inMemoryDatabaseBuilder(
            context,
            AgentDatabase::class.java
        ).build()
        agentSessionDao = agentDatabase.agentSessionDao()
    }

    @After
    fun tearDown() {
        agentDatabase.close()
    }

    @Test
    fun insertSession() = runBlocking {
        val session = AgentSessionEntity()
        agentSessionDao.addAgentSession(session)
        val sessions: Flow<List<AgentSessionEntity>> = agentSessionDao.getSessions()
        val sessionsList = sessions.first()
        assertEquals(1, sessionsList.size)
    }

    @Test
    fun changeSessionStatus() = runBlocking {
        val session = AgentSessionEntity()
        agentSessionDao.addAgentSession(session)
        agentSessionDao.changeAgentSessionStatus(
            session.sessionId,
            AgentSessionEntity.SessionStatus.PAUSED
        )
        val sessions: Flow<List<AgentSessionEntity>> = agentSessionDao.getSessions()
        val sessionsList = sessions.first()
        assertEquals(AgentSessionEntity.SessionStatus.PAUSED, sessionsList[0].status)
    }

    @Test
    fun deleteSession() = runBlocking {
        val session = AgentSessionEntity()
        agentSessionDao.addAgentSession(session)
        agentSessionDao.deleteAgentSession(session.sessionId)
        val sessions: Flow<List<AgentSessionEntity>> = agentSessionDao.getSessions()
        val sessionsList = sessions.first()
        assertEquals(0, sessionsList.size)
    }
}