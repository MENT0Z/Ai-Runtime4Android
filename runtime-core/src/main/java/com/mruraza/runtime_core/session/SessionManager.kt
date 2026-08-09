package com.mruraza.runtime_core.session

import com.mruraza.common.model.AgentSession
import com.mruraza.common.repo.AgentSessionRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Manages the lifecycle of agent sessions.
 *
 * This class acts as the domain/business layer over AgentSessionRepo].
 * Callers should interact with this manager rather than directly modifying
 * session state through the repository.
 */
class SessionManager(
    private val sessionRepo: AgentSessionRepo,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    /**
     * Starts a new agent session.
     *
     * A new session is always created with AgentSession.SessionStatus.ACTIVE].
     *
     * @param metadata JSON metadata associated with the session.
     * @return the newly created session.
     */
    suspend fun startSession(
        metadata: String = "{}"
    ): AgentSession {
        val session = AgentSession(
            sessionId = UUID.randomUUID().toString(),
            status = AgentSession.SessionStatus.ACTIVE,
            createdAt = clock(),
            metadata = metadata
        )

        sessionRepo.addAgentSession(session)

        return session
    }

    /**
     * Resumes a paused session.
     *
     * Valid transition:
     * PAUSED -> ACTIVE
     *
     * @throws SessionNotFoundException if the session does not exist.
     * @throws InvalidSessionStateException if the session cannot be resumed.
     */
    suspend fun resumeSession(
        sessionId: String
    ): AgentSession {
        val session = getRequiredSession(sessionId)

        when (session.status) {
            AgentSession.SessionStatus.PAUSED -> {
                sessionRepo.changeAgentSessionStatus(
                    sessionId = sessionId,
                    status = AgentSession.SessionStatus.ACTIVE
                )
            }

            AgentSession.SessionStatus.ACTIVE -> {
                // Already active. No-op.
                return session
            }

            AgentSession.SessionStatus.EXPIRED -> {
                throw InvalidSessionStateException(
                    sessionId = sessionId,
                    currentStatus = session.status,
                    operation = "resume"
                )
            }
        }

        return session.copy(
            status = AgentSession.SessionStatus.ACTIVE
        )
    }

    /**
     * Pauses an active session.
     *
     * Valid transition:
     * ACTIVE -> PAUSED
     *
     * @throws SessionNotFoundException if the session does not exist.
     * @throws InvalidSessionStateException if the session cannot be paused.
     */
    suspend fun pauseSession(
        sessionId: String
    ): AgentSession {
        val session = getRequiredSession(sessionId)

        when (session.status) {
            AgentSession.SessionStatus.ACTIVE -> {
                sessionRepo.changeAgentSessionStatus(
                    sessionId = sessionId,
                    status = AgentSession.SessionStatus.PAUSED
                )
            }

            AgentSession.SessionStatus.PAUSED -> {
                // Already paused. No-op.
                return session
            }

            AgentSession.SessionStatus.EXPIRED -> {
                throw InvalidSessionStateException(
                    sessionId = sessionId,
                    currentStatus = session.status,
                    operation = "pause"
                )
            }
        }

        return session.copy(
            status = AgentSession.SessionStatus.PAUSED
        )
    }

    /**
     * Ends a session.
     *
     * Since [AgentSession.SessionStatus] does not currently contain an
     * ENDED state, EXPIRED is used to represent a terminated session.
     *
     * Valid transitions:
     * ACTIVE -> EXPIRED
     * PAUSED -> EXPIRED
     *
     * Calling endSession() on an already expired session is idempotent.
     *
     * @throws SessionNotFoundException if the session does not exist.
     */
    suspend fun endSession(
        sessionId: String
    ): AgentSession {
        val session = getRequiredSession(sessionId)

        if (session.status == AgentSession.SessionStatus.EXPIRED) {
            return session
        }

        sessionRepo.changeAgentSessionStatus(
            sessionId = sessionId,
            status = AgentSession.SessionStatus.EXPIRED
        )

        return session.copy(
            status = AgentSession.SessionStatus.EXPIRED
        )
    }

    /**
     * Returns the currently active session.
     *
     * If multiple active sessions exist, the first session emitted by the
     * repository is returned.
     *
     * Ideally, the repository/database should enforce at most one active
     * session if the application requires a single active session globally.
     */
    suspend fun getActiveSession(): AgentSession? {
        return sessionRepo
            .getActiveSessions()
            .first()
            .firstOrNull()
    }

    /**
     * Observes the currently active sessions.
     *
     * Useful when the caller needs to react to session state changes.
     */
    fun observeActiveSessions(): Flow<List<AgentSession>> {
        return sessionRepo.getActiveSessions()
    }

    /**
     * Observes all sessions.
     */
    fun observeSessions(): Flow<List<AgentSession>> {
        return sessionRepo.getSessions()
    }

    /**
     * Returns a session by ID, or null if it does not exist.
     */
    suspend fun getSession(
        sessionId: String
    ): AgentSession? {
        return sessionRepo.getSessionById(sessionId)
    }

    /**
     * Returns a session by ID.
     *
     * @throws SessionNotFoundException if the session does not exist.
     */
    suspend fun getRequiredSession(
        sessionId: String
    ): AgentSession {
        return sessionRepo.getSessionById(sessionId)
            ?: throw SessionNotFoundException(sessionId)
    }

    /**
     * Deletes a session permanently.
     *
     * This should generally be used for cleanup rather than normal session
     * termination. Use [endSession] when the session lifecycle should be
     * preserved.
     */
    suspend fun deleteSession(
        sessionId: String
    ) {
        getRequiredSession(sessionId)
        sessionRepo.deleteAgentSession(sessionId)
    }
}

/**
 * Thrown when an operation references a session that does not exist.
 */
class SessionNotFoundException(
    val sessionId: String
) : IllegalStateException(
    "Session not found: $sessionId"
)

/**
 * Thrown when an invalid lifecycle transition is requested.
 */
class InvalidSessionStateException(
    val sessionId: String,
    val currentStatus: AgentSession.SessionStatus,
    val operation: String
) : IllegalStateException(
    "Cannot $operation session '$sessionId' while session is " +
            "in state $currentStatus"
)