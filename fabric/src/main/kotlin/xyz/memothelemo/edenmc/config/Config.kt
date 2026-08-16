package xyz.memothelemo.edenmc.config

import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EdenModConfig(
    val gateway: GatewayConfig?,
    val reminders: RemindersConfig = RemindersConfig(),
    @Serializable(with = TimeZoneSerializer::class)
    val timezone: TimeZone = TimeZone.UTC
)

@Serializable
data class RemindersConfig(
    @Serializable(with = CronSerializer::class)
    val sleep: Cron = CronParser(CronDefinitionBuilder
        .instanceDefinitionFor(CronType.UNIX))
        .parse("*/15 0 * * *"),
)

@Serializable
data class GatewayConfig(
    @SerialName("base_url")
    val baseUrl: String,

    @SerialName("certificate_file")
    val certificateFile: String? = null,

    @SerialName("insecure")
    val insecure: Boolean = false,

    @SerialName("token")
    val token: String,
) {
    init {
        require(baseUrl.isNotBlank()) {
            "[gateway.base_url] must not be blank. Please set it to " +
                "your hosted Eden's Gateway API's URL."
        }
        require(baseUrl.startsWith("https://")) {
            "[gateway.base_url] must start with `https://`."
        }
        if (certificateFile != null) {
            require(certificateFile.isNotBlank()) {
                "[gateway.certificate_file] must not be blank. Please set to where the " +
                    "certificate PEM file is, to be able to connect to your hosted Eden's Gateway API."
            }
        }
        require(token.isNotBlank()) {
            "[gateway.token] must not be blank. Set it to your gateway's shared " +
                "Minecraft server secret token"
        }
    }
}
