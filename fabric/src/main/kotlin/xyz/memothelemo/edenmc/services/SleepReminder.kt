package xyz.memothelemo.edenmc.services

import xyz.memothelemo.edenmc.config.EdenModConfig
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

class SleepReminder(config: EdenModConfig) : CronJob(config.reminders.sleep, config) {
    private val reminderMessage = Component
        .literal("Playing games overnight is bad for your health, so please take care of your health and sleep now.")
        .withStyle(ChatFormatting.YELLOW)

    override fun execute(server: MinecraftServer) {
        server.playerList.broadcastSystemMessage(reminderMessage, false)
    }
}
