package xyz.memothelemo.edenmc.commands

import xyz.memothelemo.edenmc.EdenMod
import xyz.memothelemo.edenmc.gateway.GatewayClient
import xyz.memothelemo.edenmc.sessions.MemberSession
import xyz.memothelemo.edenmc.sessions.getSession
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.level.ServerPlayer

private val errorEdenIsOffline = SimpleCommandExceptionType({
    "Eden is offline. Please try again later."
})

fun ServerPlayer.getMemberSession(): MemberSession
    = when (val session = this.getSession()) {
        is MemberSession -> session
        else -> throw SimpleCommandExceptionType({ "The specified player is not a member!" }).create()
    }

fun gateway(): GatewayClient =
    EdenMod.gateway ?: throw errorEdenIsOffline.create()
