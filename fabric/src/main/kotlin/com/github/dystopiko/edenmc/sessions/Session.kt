package xyz.memothelemo.edenmc.sessions

import xyz.memothelemo.edenmc.exceptions.InternalException
import xyz.memothelemo.edenmc.gateway.members.EncodedMember
import net.minecraft.world.level.GameType
import java.time.OffsetDateTime
import java.util.UUID

abstract class Session(val uuid: UUID) {
    abstract fun gameType(): GameType
}

class MemberSession: Session {
    var lastLoginAt: OffsetDateTime? = null
    val member: EncodedMember
    val rank: String
    var perks: List<String>

    constructor(
        uuid: UUID,
        lastLoginAt: OffsetDateTime?,
        member: EncodedMember,
        perks: List<String>
    ): super(uuid) {
        if (member.rank == null) {
            throw InternalException("rank field should be included in POST /sessions")
        }
        this.lastLoginAt = lastLoginAt
        this.member = member
        this.rank = member.rank
        this.perks = perks
    }

    override fun gameType(): GameType = GameType.SURVIVAL
}

class GuestSession(uuid: UUID): Session(uuid) {
    override fun gameType(): GameType = GameType.ADVENTURE
}
