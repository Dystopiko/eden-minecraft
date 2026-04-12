package com.github.dystopiko.edenmc.commands.subcommands.admin

import com.github.dystopiko.edenmc.EdenMod
import com.github.dystopiko.edenmc.EdenMod.logger
import com.github.dystopiko.edenmc.commands.BuildableCommand
import com.github.dystopiko.edenmc.commands.gateway
import com.github.dystopiko.edenmc.commands.getMemberSession
import com.github.dystopiko.edenmc.exceptions.GatewayException
import com.github.dystopiko.edenmc.gateway.GatewayErrorCode
import com.github.dystopiko.edenmc.utility.CommandContext
import com.github.dystopiko.edenmc.utility.LiteralCommandNode
import com.github.dystopiko.edenmc.utility.appendSpace
import com.mojang.brigadier.arguments.LongArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument

object FetchMemberCommand: BuildableCommand {
    override fun build(): LiteralCommandNode {
        return Commands.literal("fetch_member")
            .then(Commands
                .argument("player", EntityArgument.player())
                .executes { ctx ->
                    val player = EntityArgument.getPlayer(ctx, "player")
                    val id = player.getMemberSession().member.id
                    return@executes fetchMemberInfoBySnowflake(ctx, id)
                }
            )
            // Twilight's snowflake model requires non-zero value
            .then(Commands
                .argument("id", LongArgumentType.longArg(0))
                .executes { ctx ->
                    val id = LongArgumentType.getLong(ctx, "id")
                    return@executes fetchMemberInfoBySnowflake(ctx, id.toString())
                }
            )
            .build()
    }

    private fun fetchMemberInfoBySnowflake(ctx: CommandContext, id: String): Int {
        val gateway = gateway()
        try {
            var component = EdenMod.componentPrefix
                .appendSpace(Component.text("Fetching member information"))
                .append(Component.text("(this may take a while)").color(NamedTextColor.GRAY))
                .append(Component.text("..."))

            ctx.source.sendMessage(component)

            val member = gateway.fetchFullMember(id)
            component = Component.text()
                .append(Component.text(member.name).color(NamedTextColor.YELLOW))
                .append(Component.text(":").color(NamedTextColor.GRAY))
                .appendNewline()
                // > Discord ID: <ID>
                .appendSpace(Component.text(">").color(NamedTextColor.GRAY))
                .append(Component.text("Discord ID").color(NamedTextColor.GOLD))
                .appendSpace(Component.text(":").color(NamedTextColor.GRAY))
                .append(Component.text(member.id))
                .appendNewline()
                // > Rank: <RANK>
                .appendSpace(Component.text(">").color(NamedTextColor.GRAY))
                .append(Component.text("Rank").color(NamedTextColor.GOLD))
                .appendSpace(Component.text(":").color(NamedTextColor.GRAY))
                .append(Component.text(member.rank))
                .appendNewline()
                // > Invited by: <INVITED_BY>
                .appendSpace(Component.text(">").color(NamedTextColor.GRAY))
                .append(Component.text("Invited by").color(NamedTextColor.GOLD))
                .appendSpace(Component.text(":").color(NamedTextColor.GRAY))
                .append(Component.text(member.invitedBy?.name ?: "<none>"))
                .build()

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

            logger.warn("Request to get member information", e)
            ctx.source.sendMessage(component)
        }
        return 1
    }
}
