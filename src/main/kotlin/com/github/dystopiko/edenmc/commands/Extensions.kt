package com.github.dystopiko.edenmc.commands

import com.github.dystopiko.edenmc.EdenMod
import com.github.dystopiko.edenmc.gateway.GatewayClient
import com.github.dystopiko.edenmc.utility.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType

private val errorEdenIsOffline = SimpleCommandExceptionType({
    "Eden is offline. Please try again later."
})

fun gateway(): GatewayClient = EdenMod.gateway ?: throw errorEdenIsOffline.create()
