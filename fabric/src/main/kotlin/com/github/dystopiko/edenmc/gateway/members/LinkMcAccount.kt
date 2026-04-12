package com.github.dystopiko.edenmc.gateway.members

import com.github.dystopiko.edenmc.gateway.McUUIDSerializer
import com.github.dystopiko.edenmc.gateway.Rfc3339Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Request body for `POST /members/{id}/link/minecraft`.
 */
@Serializable
data class LinkMcAccount (
    @Serializable(with = McUUIDSerializer::class)
    val uuid: UUID,
    val username: String,
    val ip: String,
    val java: Boolean
)

/**
 * Response body for `POST /members/{id}/link/minecraft`.
 */
@Serializable
data class LinkChallenge (
    val code: String,

    @SerialName("expires_at")
    @Serializable(with = Rfc3339Serializer::class)
    val expiresAt: OffsetDateTime?,
)
