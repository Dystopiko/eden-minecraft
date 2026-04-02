package com.github.dystopiko.edenmc.commands.subcommands

import com.github.dystopiko.edenmc.EdenMod
import com.github.dystopiko.edenmc.EdenMod.logger
import com.github.dystopiko.edenmc.commands.BuildableCommand
import com.github.dystopiko.edenmc.commands.gateway
import com.github.dystopiko.edenmc.exceptions.GatewayException
import com.github.dystopiko.edenmc.utility.CommandContext
import com.github.dystopiko.edenmc.utility.LiteralCommandNode
import com.github.dystopiko.edenmc.utility.appendSpace
import com.github.dystopiko.edenmc.utility.resolveIpAddress
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.Commands

object LinkCommand: BuildableCommand {
    override fun build(): LiteralCommandNode = Commands.literal("link")
        .executes(::requestLink)
        .build()

    private fun requestLink(ctx: CommandContext): Int {
        val gateway = gateway()
        val player = ctx.source.playerOrException

        val uuid = player.uuid
        val ip = player.resolveIpAddress()
        val username = player.plainTextName

        try {
            val response = gateway.linkAccount(uuid, username, ip, java = true)
            val component = EdenMod.componentPrefix
                .appendSpace(Component.text("Please send this exact code:"))
                .appendSpace(
                    Component.text(response.code)
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.copyToClipboard(response.code))
                )
                .appendSpace(Component.text("to Eden Discord bot in DMs."))

            ctx.source.sendMessage(component)
        } catch (e: GatewayException) {
            val component = EdenMod.componentPrefix
                .append(Component.text(e.errorMessage).color(NamedTextColor.RED))

            ctx.source.sendMessage(component)
        } catch (e: Exception) {
            val component = EdenMod.componentPrefix
                .append(Component.text("Failed to process command. Please try again.")
                .color(NamedTextColor.RED))

            logger.warn("Request to link account failed", e)
            ctx.source.sendMessage(component)
        }

        return 0
    }
}
