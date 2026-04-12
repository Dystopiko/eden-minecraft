package com.github.dystopiko.edenmc.utility

import com.github.dystopiko.edenmc.interfaces.ServerPlayerAccessor
import com.github.dystopiko.edenmc.sessions.getMemberRank
import com.mojang.authlib.GameProfile
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.server.MinecraftServer
import net.minecraft.network.chat.Component as McComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

private fun displayNameForChatCommon(
    profile: GameProfile,
    rawRank: String,
    server: MinecraftServer
): McComponent {
    val rank = rawRank.uppercase()
    val color = when (rank) {
        "ADMIN" -> NamedTextColor.YELLOW
        "STAFF" -> NamedTextColor.BLUE
        "CONTRIBUTOR" -> NamedTextColor.LIGHT_PURPLE
        "GUEST" -> NamedTextColor.DARK_GREEN
        else -> NamedTextColor.WHITE
    }

    val displayName = Component.text()
        .append(Component.text(rank).color(color))
        .appendSpace()
        .append(Component.text(profile.name))
        .build()

    val controller = MinecraftServerAudiences.of(server)
    return controller.asNative(displayName)
}

fun Player.displayNameForChat(): McComponent
    = displayNameForChatCommon(
        this.gameProfile,
        this.getMemberRank(),
        (this as ServerPlayerAccessor).`eden$getServer`()
    )

fun ServerPlayer.displayNameForChat(): McComponent
    = displayNameForChatCommon(
        this.gameProfile,
        this.getMemberRank(),
        (this as ServerPlayerAccessor).`eden$getServer`()
    )
