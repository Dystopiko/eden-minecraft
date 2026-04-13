package xyz.memothelemo.edenmc.gateway.admin

import xyz.memothelemo.edenmc.gateway.members.EncodedMember
import kotlinx.serialization.Serializable

@Serializable
data class Invitees(
    val count: Long,
    val invitees: List<EncodedMember>
)
