package com.github.dystopiko.edenmc.gateway.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatchSettings(
    @SerialName("allow_guests")
    val allowGuests: Boolean? = null
)
