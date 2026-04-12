package com.github.dystopiko.edenmc.gateway.members

import kotlinx.serialization.Serializable

/**
 * Encoded Dystopia member
 */
@Serializable
data class EncodedMember (
    /** Member's Discord user ID*/
    val id: String,

    /** Member's Discord username */
    val name: String,

    /** Member's current rank in primary guild */
    val rank: String? = null
)
