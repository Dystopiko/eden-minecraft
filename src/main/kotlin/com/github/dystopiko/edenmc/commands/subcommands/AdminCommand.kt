package com.github.dystopiko.edenmc.commands.subcommands

import com.github.dystopiko.edenmc.commands.BuildableCommand
import com.github.dystopiko.edenmc.commands.subcommands.admin.AllowGuests
import com.github.dystopiko.edenmc.commands.subcommands.admin.FetchMemberCommand
import com.github.dystopiko.edenmc.commands.subcommands.admin.GetInviteesCommand
import com.github.dystopiko.edenmc.sessions.MemberSession
import com.github.dystopiko.edenmc.sessions.getSession
import com.github.dystopiko.edenmc.utility.LiteralCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object AdminCommand: BuildableCommand {
    override fun build(): LiteralCommandNode {
        val root = Commands.literal("admin")
            .requires(::requiresDystopiaAdmin)
            .build()

        root.addChild(AllowGuests.build())
        root.addChild(FetchMemberCommand.build())
        root.addChild(GetInviteesCommand.build())
        return root
    }

    private fun requiresDystopiaAdmin(source: CommandSourceStack): Boolean {
        val player = source.player ?: return true
        return when (val session = player.getSession()) {
            is MemberSession -> session.rank == "admin"
            else -> false
        }
    }
}
