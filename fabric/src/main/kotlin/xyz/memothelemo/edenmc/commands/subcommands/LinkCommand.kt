package xyz.memothelemo.edenmc.commands.subcommands

import xyz.memothelemo.edenmc.EdenMod
import xyz.memothelemo.edenmc.EdenMod.logger
import xyz.memothelemo.edenmc.commands.BuildableCommand
import xyz.memothelemo.edenmc.commands.gateway
import xyz.memothelemo.edenmc.exceptions.GatewayException
import xyz.memothelemo.edenmc.utility.CommandContext
import xyz.memothelemo.edenmc.utility.LiteralCommandNode
import xyz.memothelemo.edenmc.utility.appendSpace
import xyz.memothelemo.edenmc.utility.resolveIpAddress
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.Commands
import org.geysermc.floodgate.api.FloodgateApi

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
        val isJava = !FloodgateApi.getInstance().isFloodgatePlayer(uuid)

        try {
            val response = gateway.linkAccount(uuid, username, ip, java = isJava)
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
