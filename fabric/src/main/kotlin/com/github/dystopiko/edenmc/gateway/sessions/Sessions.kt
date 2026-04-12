package com.github.dystopiko.edenmc.gateway.sessions

import com.github.dystopiko.edenmc.gateway.McUUIDSerializer
import com.github.dystopiko.edenmc.gateway.Rfc3339Serializer
import com.github.dystopiko.edenmc.gateway.members.EncodedMember
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Request body for `POST /sessions`.
 */
@Serializable
data class RequestSession(
    @Serializable(with = McUUIDSerializer::class)
    val uuid: UUID,
    val ip: String,
    val java: Boolean,
)

/**
 * Response body for `POST /sessions`.
 */
@Serializable
data class SessionGranted(
    @SerialName("last_login_at")
    @Serializable(with = Rfc3339Serializer::class)
    val lastLoginAt: OffsetDateTime?,
    val member: EncodedMember?,
    val perks: List<String>,
)

/**
 * Request body for `POST /sessions/validate`.
 */
@Serializable
data class ValidatePlayers(
    val players: List<String>,
)

/**
 * Response body for `POST /sessions/validate`.
 */
@Serializable
data class ValidatePlayersResponse(
    val players: Map<String, PlayerEntry?>,
)

@Serializable
data class PlayerEntry(
    val member: EncodedMember,
    val perks: List<String>,
)
