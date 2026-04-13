package xyz.memothelemo.edenmc

import xyz.memothelemo.edenmc.api.impl.EdenImpl
import xyz.memothelemo.edenmc.config.ConfigFileLoader
import xyz.memothelemo.edenmc.commands.registerCommands
import xyz.memothelemo.edenmc.gateway.GatewayClient
import xyz.memothelemo.edenmc.listeners.registerCommandListeners
import xyz.memothelemo.edenmc.listeners.registerPlayerListeners
import xyz.memothelemo.edenmc.services.ExpandWorldBorder
import xyz.memothelemo.edenmc.services.SleepReminder
import xyz.memothelemo.edenmc.utility.injectApiImpl
import xyz.memothelemo.edenmc.utility.setMinLevel
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.annotations.Nullable
import java.util.concurrent.Executors

object EdenMod : DedicatedServerModInitializer {
    const val ID = "eden"

    val componentPrefix: TextComponent = Component.text()
        .append(Component.text("[Eden] ")
            .color(NamedTextColor.LIGHT_PURPLE)
            .decorate(TextDecoration.BOLD)
        )
        .build()

    val debugMode: Boolean = FabricLoader.getInstance().isDevelopmentEnvironment
        || System.getProperty("eden.debug") != null

    val executor = Executors.newWorkStealingPool(8)

    @JvmField @Nullable
    var gateway: GatewayClient? = null

    @JvmField
    val logger: Logger = LogManager.getLogger("EdenMC")

    override fun onInitializeServer() {
        injectApiImpl(EdenImpl())

        if (debugMode) {
            logger.setMinLevel(Level.DEBUG)
            logger.warn("Debug mode is enabled")
        }

        val configFile = FabricLoader.getInstance().configDir.resolve(ConfigFileLoader.FILE_NAME)
        val config = ConfigFileLoader(configFile).load()
        if (config.gateway == null) {
            logger.warn("Gateway is not configured! Eden will run in offline mode")
        } else {
            gateway = GatewayClient(config.gateway)
        }

        registerCommandListeners()
        registerPlayerListeners()

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> registerCommands(dispatcher) }
        ExpandWorldBorder(config).start()
        SleepReminder(config).start()

        logger.info("EdenMC has initialized successfully")
    }
}
