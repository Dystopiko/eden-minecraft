package com.github.dystopiko.edenmc.services

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import com.github.dystopiko.edenmc.EdenMod
import com.github.dystopiko.edenmc.config.EdenModConfig
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.kyori.adventure.text.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth

private val expandCron = CronParser(CronDefinitionBuilder
    .instanceDefinitionFor(CronType.UNIX))
    .parse("07 18 */3 * *")

private const val INTERVAL_IN_BLOCKS = 128
private const val MAXIMUM_BORDER_SIZE: Double = 10_000.0

class ExpandWorldBorder(config: EdenModConfig) : CronJob(expandCron, config) {
    // Expanding the world border by 128 blocks every time it is executed in all levels (dimensions)
    override fun execute(server: MinecraftServer) {
        var didExpandWorldBorder = false
        for (level in server.allLevels) {
            didExpandWorldBorder = didExpandWorldBorder || expandBorderForLevel(level)
        }

        if (!didExpandWorldBorder) return

        val controller = MinecraftServerAudiences.of(server)
        val component = EdenMod.componentPrefix.append(Component.text("Expanded world border."))
        server.playerList.broadcastSystemMessage(controller.asNative(component), false)
    }

    private fun expandBorderForLevel(level: ServerLevel): Boolean {
        val border = level.worldBorder
        val prev = border.size
        if (prev >= MAXIMUM_BORDER_SIZE) return false

        // 100L = 5 seconds * 20 ticks/second
        val next = Mth.clamp(prev + INTERVAL_IN_BLOCKS, 1.0, MAXIMUM_BORDER_SIZE)
        border.lerpSizeBetween(prev, next, 100L, level.gameTime)
        return true
    }
}
