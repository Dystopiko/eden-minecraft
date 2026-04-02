package com.github.dystopiko.edenmc.exceptions

import com.github.dystopiko.edenmc.EdenMod.ID
import net.fabricmc.loader.api.FabricLoader

@Suppress("unused")
class InternalException : RuntimeException {
    constructor() : super(buildMessage())
    constructor(message: String) : super(buildMessage(message))
    constructor(message: String, cause: Throwable) : super(buildMessage(message), cause)

    companion object {
        private val issuesUrl: String by lazy {
            FabricLoader.getInstance()
                .getModContainer(ID)
                .orElseThrow { IllegalStateException("Mod container '${ID}' does not exist!") }
                .metadata.contact.get("issues")
                .orElseThrow { IllegalStateException("No 'issues' URL in metadata for '${ID}'") }
        }

        private fun buildMessage(detail: String? = null): String {
            val base = "An internal error occurred! Please report this at $issuesUrl"
            return if (detail != null) "$base\nDetails: $detail" else base
        }
    }
}
