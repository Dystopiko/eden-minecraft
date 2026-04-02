package com.github.dystopiko.edenmc.gateway

import kotlinx.serialization.Serializable

// Reference: https://github.com/Dystopiko/eden/blob/71d61c37bfda9414c28afdddd3e2e84088eab13c/src/gateway-server/src/errors/mod.rs#L15-L37
@Serializable
data class GatewayError (
    @Serializable(with = GatewayErrorCodeSerializer::class)
    val code: GatewayErrorCode,
    val message: String
)

sealed class GatewayErrorCode(val value: String) {
    data object Internal: GatewayErrorCode("INTERNAL")
    data object ReadonlyMode: GatewayErrorCode("READONLY_MODE")
    data object NotFound: GatewayErrorCode("NOT_FOUND")
    data object InvalidRequest: GatewayErrorCode("INVALID_REQUEST")
    data object ServiceUnavailable: GatewayErrorCode("SERVICE_UNAVAILABLE")
    data object RateLimited: GatewayErrorCode("RATE_LIMITED")
    data class Unknown(val raw: String) : GatewayErrorCode(raw)

    override fun equals(other: Any?): Boolean = other is GatewayErrorCode
        && this.value == other.value

    override fun hashCode(): Int = this.value.hashCode()
}
