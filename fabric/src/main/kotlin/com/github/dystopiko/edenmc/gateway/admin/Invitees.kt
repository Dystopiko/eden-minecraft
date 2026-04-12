package com.github.dystopiko.edenmc.gateway.admin

import com.github.dystopiko.edenmc.gateway.members.EncodedMember
import kotlinx.serialization.Serializable

@Serializable
data class Invitees(
    val count: Long,
    val invitees: List<EncodedMember>
)
