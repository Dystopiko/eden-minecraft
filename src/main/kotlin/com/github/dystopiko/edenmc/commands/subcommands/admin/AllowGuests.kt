package com.github.dystopiko.edenmc.commands.subcommands.admin

import com.github.dystopiko.edenmc.EdenMod
import com.github.dystopiko.edenmc.EdenMod.logger
import com.github.dystopiko.edenmc.commands.BuildableCommand
import com.github.dystopiko.edenmc.commands.gateway
import com.github.dystopiko.edenmc.exceptions.GatewayException
import com.github.dystopiko.edenmc.gateway.admin.PatchSettings
import com.github.dystopiko.edenmc.utility.CommandContext
import com.github.dystopiko.edenmc.utility.LiteralCommandNode
import com.github.dystopiko.edenmc.utility.appendSpace
import com.mojang.brigadier.arguments.BoolArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.Commands

object AllowGuests: BuildableCommand {
    override fun build(): LiteralCommandNode {
        return Commands.literal("allow_guests")
            .then(Commands
                .argument("enabled", BoolArgumentType.bool())
                .executes(::setAllowGuests)
            )
            .build()
    }

    private fun setAllowGuests(ctx: CommandContext): Int {
        val enabled = BoolArgumentType.getBool(ctx, "enabled")
        val gateway = gateway()
        try {
            var component = EdenMod.componentPrefix
                .appendSpace(Component.text("Setting"))
                .appendSpace(Component.text("allow_guests").color(NamedTextColor.BLUE))
                .appendSpace(Component.text("to"))
                .appendSpace(Component.text(enabled).color(NamedTextColor.YELLOW))
                .append(Component.text("(this may take a while)").color(NamedTextColor.GRAY))
                .append(Component.text("..."))

            ctx.source.sendMessage(component)
            gateway.patchSettings(PatchSettings(allowGuests = enabled))

            component = EdenMod.componentPrefix
                .appendSpace(Component.text()
                    .color(NamedTextColor.GREEN)
                    .append(Component.text("Successfully set allow_guests to $enabled."))
                    .build()
                )

            ctx.source.sendMessage(component)
        } catch (e: GatewayException) {
            val component = Component
                .text(e.message ?: "Could not set allow_guests to $enabled")
                .color(NamedTextColor.RED)

            ctx.source.sendMessage(component)
        } catch (e: Exception) {
            val component = Component.text("Failed to process command. Please try again.")
                .color(NamedTextColor.RED)

            logger.warn("Request to set allow_guests failed!", e)
            ctx.source.sendMessage(component)
        }
        return 1
    }
}
