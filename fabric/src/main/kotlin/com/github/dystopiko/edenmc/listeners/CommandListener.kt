package com.github.dystopiko.edenmc.listeners;

import com.github.dystopiko.edenmc.EdenMod
import com.github.dystopiko.edenmc.EdenMod.logger
import com.github.dystopiko.edenmc.callbacks.CommandExecutionCallback
import com.github.dystopiko.edenmc.exceptions.GatewayException
import com.github.dystopiko.edenmc.gateway.alerts.CommandExecutor
import com.github.dystopiko.edenmc.utility.CommandContext
import net.minecraft.server.permissions.Permissions

fun registerCommandListeners() {
    CommandExecutionCallback.EVENT.register(::logCommands)
}

private val blacklistedCommands = setOf(
    // `/w` and `/tell` redirect to `/msg`
    "msg", "teammsg", "help", "list", "random", "me",
)

fun logCommands(ctx: CommandContext) {
    val name = getCanonicalName(ctx)
    if (name == "eden") {
        val usesAdminSubcommand = ctx.nodes.any { it.node.name.contains("admin") }
        if (!usesAdminSubcommand) return
    } else if (blacklistedCommands.contains(name)) return

    // To prevent spam sent by regular players, we'll limit the scope to admins only
    if (!ctx.source.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) return

    val gateway = EdenMod.gateway ?: return
    val command = ctx.input
    val player = ctx.source.player

    var commandExecutor: CommandExecutor = CommandExecutor.Console
    if (player != null) {
        val dimension = player.level().dimension().identifier().toString()
        val gameType = player.gameMode.gameModeForPlayer.serializedName
        commandExecutor = CommandExecutor.Player(
            dimension = dimension,
            gamemode = gameType,
            position = player.blockPosition(),
            username = player.plainTextName,
            uuid = player.uuid
        )
    }

    EdenMod.executor.execute {
        try {
            gateway.logCommandAlert("/$command", commandExecutor)
            return@execute
        } catch (e: GatewayException) {
            logger.warn("Received error from gateway while trying to send command use alert: {}", e.errorMessage)
        } catch (e: Exception) {
            logger.warn("Failed to send alert to gateway", e)
        } finally {
            logger.warn("{} used command `{}`", commandExecutor, command)
        }
    }
}

private fun getCanonicalName(ctx: CommandContext): String {
    val root = ctx.source.server.commands.dispatcher.root

    return when (root == ctx.rootNode) {
        // If the root node goes to the actual root node from the
        // dispatcher return the first node's name, since the redirected
        // node is considered as the root node in `ctx.rootNode`
        true -> ctx.nodes.first().node.name
        false -> ctx.rootNode.name
    }
}
