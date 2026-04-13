package xyz.memothelemo.edenmc.gateway.alerts

import xyz.memothelemo.edenmc.gateway.McBlockPosSerializer
import xyz.memothelemo.edenmc.gateway.McUUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import java.util.UUID

/**
 * Request body for `POST /alerts/admin_commands`.
 */
@Serializable
data class AlertAdminCommandUse (
    val command: String,
    val executor: CommandExecutor
)

@Serializable
sealed class CommandExecutor {
    @Serializable
    @SerialName("console")
    data object Console : CommandExecutor()

    @Serializable
    @SerialName("player")
    data class Player(
        val dimension: String,
        val gamemode: String,

        @Serializable(with = McBlockPosSerializer::class)
        val position: BlockPos,
        val username: String,

        @Serializable(with = McUUIDSerializer::class)
        val uuid: UUID
    ) : CommandExecutor()
}
