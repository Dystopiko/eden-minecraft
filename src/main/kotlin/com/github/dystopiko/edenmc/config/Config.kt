package com.github.dystopiko.edenmc.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EdenModConfig(
    val gateway: GatewayConfig?,
)

@Serializable
data class GatewayConfig(
    @SerialName("base_url")
    val baseUrl: String,

    @SerialName("token")
    val token: String,
) {
    init {
        require(baseUrl.isNotBlank()) {
            "[gateway.base_url] must not be blank. Please set it to " +
                "your hosted Eden's Gateway API's URL."
        }
        require(baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            "[gateway.base_url] must start with `http://` or `https://`."
        }
        require(token.isNotBlank()) {
            "[gateway.token] must not be blank. Set it to your gateway's shared " +
                "Minecraft server secret token"
        }
    }
}
