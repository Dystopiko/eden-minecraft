package com.github.dystopiko.edenmc.utility

import com.github.dystopiko.edenmc.exceptions.InternalException
import com.google.common.net.InetAddresses
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.minecraft.network.Connection
import net.minecraft.server.level.ServerPlayer
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import java.net.InetSocketAddress
import java.net.SocketAddress

fun TextComponent.Builder.appendSpace(component: Component): TextComponent.Builder
    = this.append(component).append(Component.text(" "))

fun TextComponent.appendSpace(component: Component): TextComponent
    = this.append(component).append(Component.text(" "))

fun Logger.setMinLevel(level: Level) {
    val core = (this as? org.apache.logging.log4j.core.Logger
        ?: error("Expected a Log4j CoreLogger but got ${this::class.qualifiedName}"))
        as org.apache.logging.log4j.core.Logger

    val appenders = core.appenders.values.toList()
    core.isAdditive = false

    Configurator.setLevel(core, level)
    appenders.forEach(core::addAppender)
}

fun Connection.resolveIpAddress(): String
    = deriveIpAddressFromSocket(this.javaClass.name, this.remoteAddress)

fun ServerPlayer.resolveIpAddress(): String
    = deriveIpAddressFromSocket(this.javaClass.name, this.connection.remoteAddress)

private fun deriveIpAddressFromSocket(className: String, socket: SocketAddress): String {
    if (socket is InetSocketAddress) {
        return InetAddresses.toAddrString(socket.address)
    }

    // It should get the IP address from the specific connection object
    throw InternalException("Unknown class case: $className class")
}
