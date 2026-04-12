package com.github.dystopiko.edenmc.callbacks

import com.github.dystopiko.edenmc.utility.CommandContext
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

fun interface CommandExecutionCallback {
    fun onCommandExecution(context: CommandContext)

    companion object {
        @JvmField
        val EVENT: Event<CommandExecutionCallback> = EventFactory.createArrayBacked(
            CommandExecutionCallback::class.java
        ) {
            listeners -> { context ->
                for (listener in listeners) listener.onCommandExecution(context)
            }
        }
    }
}
