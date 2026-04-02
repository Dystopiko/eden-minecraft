package com.github.dystopiko.edenmc.commands

import com.github.dystopiko.edenmc.utility.LiteralCommandNode

interface BuildableCommand {
    fun build(): LiteralCommandNode
}
