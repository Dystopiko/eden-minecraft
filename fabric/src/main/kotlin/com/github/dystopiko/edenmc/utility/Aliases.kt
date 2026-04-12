package com.github.dystopiko.edenmc.utility

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.commands.CommandSourceStack

typealias CommandContext = CommandContext<CommandSourceStack>
typealias CommandNode = CommandNode<CommandSourceStack>
typealias LiteralCommandNode = LiteralCommandNode<CommandSourceStack>
