package com.github.dystopiko.edenmc.sessions

import com.github.dystopiko.edenmc.exceptions.InternalException
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object SessionManager {
    private val sessions = ConcurrentHashMap<UUID, Session>()

    fun isSessionPresentForPlayer(id: UUID): Boolean
        = this.sessions.contains(id)

    fun register(id: UUID, session: Session) {
        if (this.sessions.putIfAbsent(id, session) != null) {
            throw InternalException("Attempt to register session twice!")
        }
    }

    fun removeSession(id: UUID) {
        this.sessions.remove(id)
    }

    fun getSession(id: UUID): Session = this.sessions[id]
        ?: throw InternalException("Every player must be given a session by gateway")
}

fun Player.getMemberRank(): String
    = when (val session = SessionManager.getSession(this.gameProfile.id)) {
        is MemberSession -> session.rank
        is GuestSession -> "guest"
        else -> "<unknown>"
    }

fun ServerPlayer.getMemberRank(): String
    = when (val session = this.getSession()) {
        is MemberSession -> session.rank
        is GuestSession -> "guest"
        else -> "<unknown>"
    }

fun ServerPlayer.getSession(): Session = SessionManager.getSession(this.uuid)
