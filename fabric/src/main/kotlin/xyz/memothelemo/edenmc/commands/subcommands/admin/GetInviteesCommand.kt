package xyz.memothelemo.edenmc.commands.subcommands.admin

import xyz.memothelemo.edenmc.EdenMod
import xyz.memothelemo.edenmc.EdenMod.logger
import xyz.memothelemo.edenmc.commands.BuildableCommand
import xyz.memothelemo.edenmc.commands.gateway
import xyz.memothelemo.edenmc.commands.getMemberSession
import xyz.memothelemo.edenmc.exceptions.GatewayException
import xyz.memothelemo.edenmc.gateway.GatewayErrorCode
import xyz.memothelemo.edenmc.utility.CommandContext
import xyz.memothelemo.edenmc.utility.LiteralCommandNode
import xyz.memothelemo.edenmc.utility.appendSpace
import com.mojang.brigadier.arguments.LongArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument

object GetInviteesCommand: BuildableCommand {
    override fun build(): LiteralCommandNode {
        return Commands.literal("get_invitees")
            .then(Commands
                .argument("player", EntityArgument.player())
                .executes { ctx ->
                    val player = EntityArgument.getPlayer(ctx, "player")
                    val id = player.getMemberSession().member.id
                    return@executes getInviteesCommon(ctx, id)
                }
            )
            // Twilight's snowflake model requires non-zero value
            .then(Commands
                .argument("id", LongArgumentType.longArg(0))
                .executes { ctx ->
                    val id = LongArgumentType.getLong(ctx, "id")
                    return@executes getInviteesCommon(ctx, id.toString())
                }
            )
            .build()
    }

    private fun getInviteesCommon(ctx: CommandContext, id: String): Int {
        val gateway = gateway()
        try {
            var component = EdenMod.componentPrefix
                .appendSpace(Component.text("Fetching invitees"))
                .append(Component.text("(this may take a while)").color(NamedTextColor.GRAY))
                .append(Component.text("..."))

            ctx.source.sendMessage(component)

            val response = gateway.getInvitees(id)
            var builder = Component.text()
                .append(Component.text("Invitees of $id (${response.count} total)").color(NamedTextColor.YELLOW))
                .append(Component.text(":").color(NamedTextColor.GRAY))

            for (member in response.invitees) {
                builder = builder.appendNewline()
                    .appendSpace(Component.text(">").color(NamedTextColor.GRAY))
                    .appendSpace(Component.text(member.name))
                    .append(Component.text("(${member.id})").color(NamedTextColor.GRAY))
            }

            component = builder.build()
            ctx.source.sendMessage(component)
        } catch (e: GatewayException) {
            var message = e.message
            if (e.code == GatewayErrorCode.NotFound || message == null) {
                message = "The specified member does not exists!"
            }

            val component = Component.text(message).color(NamedTextColor.RED)
            ctx.source.sendMessage(component)
        } catch (e: Exception) {
            val component = Component.text("Failed to process command. Please try again.")
                .color(NamedTextColor.RED)

            logger.warn("Request to get invitees failed!", e)
            ctx.source.sendMessage(component)
        }
        return 1
    }
}
