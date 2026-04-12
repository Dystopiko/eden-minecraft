package com.github.dystopiko.edenmc.gateway.members

import com.github.dystopiko.edenmc.gateway.McUUIDSerializer
import com.github.dystopiko.edenmc.gateway.Rfc3339Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class FullMember(
    val id: String,
    val name: String,
    val rank: String,

    @SerialName("invited_by")
    val invitedBy: EncodedMember?,

    @SerialName("last_login_at")
    @Serializable(with = Rfc3339Serializer::class)
    val lastLoginAt: OffsetDateTime?,

    @SerialName("last_account")
    @Serializable(with = McUUIDSerializer::class)
    val lastAccount: UUID?
)
