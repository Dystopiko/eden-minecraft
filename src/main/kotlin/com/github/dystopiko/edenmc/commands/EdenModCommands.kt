package com.github.dystopiko.edenmc.commands

import com.github.dystopiko.edenmc.commands.subcommands.LinkCommand
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
    val root = Commands.literal("eden").build()
    dispatcher.root.addChild(root)

    root.addChild(LinkCommand.build())
}
