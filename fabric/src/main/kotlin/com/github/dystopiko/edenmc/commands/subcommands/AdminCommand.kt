package xyz.memothelemo.edenmc.commands.subcommands

import xyz.memothelemo.edenmc.commands.BuildableCommand
import xyz.memothelemo.edenmc.commands.subcommands.admin.AllowGuests
import xyz.memothelemo.edenmc.commands.subcommands.admin.FetchMemberCommand
import xyz.memothelemo.edenmc.commands.subcommands.admin.GetInviteesCommand
import xyz.memothelemo.edenmc.sessions.MemberSession
import xyz.memothelemo.edenmc.sessions.getSession
import xyz.memothelemo.edenmc.utility.LiteralCommandNode
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
