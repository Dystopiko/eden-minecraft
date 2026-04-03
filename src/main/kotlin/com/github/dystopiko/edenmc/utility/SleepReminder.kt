package com.github.dystopiko.edenmc.utility

import com.cronutils.model.time.ExecutionTime
import com.github.dystopiko.edenmc.config.EdenModConfig
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

private val reminderMessage = Component
    .literal("Playing games overnight is bad for your health, so please take care of your health and sleep now.")
    .withStyle(ChatFormatting.YELLOW)

class SleepReminder(private val config: EdenModConfig) {
    val interval: ExecutionTime = ExecutionTime.forCron(config.reminders.sleep)
    var nextExecution: ZonedDateTime? = null

    fun start() {
        reloadNextExecution(currentTime())
        ServerTickEvents.END_SERVER_TICK.register(::tick)
    }

    private fun tick(server: MinecraftServer) {
        if (nextExecution == null) return

        val currentTime = currentTime()
        if (currentTime <= nextExecution) return

        server.playerList.broadcastSystemMessage(reminderMessage, false)
        reloadNextExecution(currentTime)
    }

    private fun reloadNextExecution(dt: ZonedDateTime) {
        this.nextExecution = interval.nextExecution(dt).getOrNull()
    }

    private fun currentTime(): ZonedDateTime
        = ZonedDateTime.now(ZoneId.of(config.timezone.id))
}
