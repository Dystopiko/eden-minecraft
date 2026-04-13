package xyz.memothelemo.edenmc.commands

import xyz.memothelemo.edenmc.utility.LiteralCommandNode

interface BuildableCommand {
    fun build(): LiteralCommandNode
}
