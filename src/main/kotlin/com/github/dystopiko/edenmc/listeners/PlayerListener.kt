package com.github.dystopiko.edenmc.listeners

import com.github.dystopiko.edenmc.EdenMod
import com.github.dystopiko.edenmc.sessions.GuestSession
import com.github.dystopiko.edenmc.sessions.MemberSession
import com.github.dystopiko.edenmc.sessions.SessionManager
import com.github.dystopiko.edenmc.sessions.getSession
import com.github.dystopiko.edenmc.utility.appendSpace
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.Node
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType

fun registerPlayerListeners() {
    ServerPlayerEvents.JOIN.register(::preparePlayer)
    ServerPlayerEvents.LEAVE.register(::cleanUnusedSession)
}

private val guestWelcomeMessage = Component.text()
    .appendSpace(
        Component.text("Welcome to Dystopia!")
            .color(NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD)
    )
    .appendSpace(Component.text("You're in"))
    .appendSpace(Component.text("adventure mode").color(NamedTextColor.AQUA))
    .appendSpace(Component.text("because you may have not been registered as a member of Dystopia."))
    .appendNewline().appendNewline()
    .appendSpace(Component.text("You may run")).appendSpace(
        Component.text("/eden link")
            .color(NamedTextColor.LIGHT_PURPLE)
            .clickEvent(ClickEvent.suggestCommand("/eden link"))
    )
    .appendSpace(Component.text("to link your Minecraft with your Discord account"))
    .append(
        Component.text("(you're required to be a member of Dystopia to do this)")
            .color(NamedTextColor.GRAY)
    )
    .append(Component.text("."))
    .build()

private fun preparePlayer(player: ServerPlayer) {
    when (val session = player.getSession()) {
        is MemberSession -> {
            val component = EdenMod.componentPrefix
                .appendSpace(Component.text("Logged in as"))
                .append(Component.text(session.member.name).color(NamedTextColor.GOLD))

            player.sendMessage(component)
            player.setGameMode(GameType.SURVIVAL)
            setupPlayerPerks(player, session.perks)
        }
        is GuestSession -> {
            player.sendMessage(guestWelcomeMessage)
            player.setGameMode(GameType.ADVENTURE)
        }
        else -> {}
    }
}

private fun setupPlayerPerks(player: ServerPlayer, perks: List<String>) {
    EdenMod.logger.debug("Applying perks for {} (perks={})", player.plainTextName, perks)
    val luckperms = LuckPermsProvider.get()
    luckperms.userManager.loadUser(player.uuid)
        .thenComposeAsync { user ->
            user.data().clear()
            for (perk in perks) {
                user.data().add(Node.builder(perk).build())
            }
            return@thenComposeAsync luckperms.userManager.saveUser(user)
        }
        .whenCompleteAsync({ _, throwable ->
            if (throwable != null) {
                EdenMod.logger.warn("Failed to setup perks for {}", player.plainTextName, throwable)
                return@whenCompleteAsync
            }
            EdenMod.logger.debug("Applied {} perk entries for {}", perks.size, player.plainTextName)
        }, EdenMod.executor)
}

private fun cleanUnusedSession(player: ServerPlayer) {
    SessionManager.removeSession(player.uuid)
}
