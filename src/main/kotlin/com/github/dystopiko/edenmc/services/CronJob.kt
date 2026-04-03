package com.github.dystopiko.edenmc.services

import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import com.github.dystopiko.edenmc.config.EdenModConfig
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

abstract class CronJob(cron: Cron, protected val config: EdenModConfig) {
    private val executionTime = ExecutionTime.forCron(cron)
    private val zoneId = ZoneId.of(config.timezone.id)

    private var nextExecution: ZonedDateTime? = null

    abstract fun execute(server: MinecraftServer)

    fun start() {
        scheduleNext(now())
        ServerTickEvents.END_SERVER_TICK.register(::onTick)
    }

    private fun onTick(server: MinecraftServer) {
        val next = nextExecution ?: return
        val current = now()
        if (current.isBefore(next)) return

        execute(server)
        scheduleNext(current)
    }

    private fun scheduleNext(from: ZonedDateTime) {
        nextExecution = executionTime.nextExecution(from).getOrNull()
    }

    protected fun now(): ZonedDateTime
        = ZonedDateTime.now(zoneId)
}
