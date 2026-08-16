package xyz.memothelemo.edenmc.config

import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import kotlinx.datetime.TimeZone
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private val definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)

object CronSerializer : KSerializer<Cron> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Crontab expression", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Cron) {
        encoder.encodeString(value.asString())
    }

    override fun deserialize(decoder: Decoder): Cron =
        CronParser(definition).parse(decoder.decodeString())
}

/** Serializes [TimeZone] as its IANA timezone ID string (e.g. `"Asia/Manila"`). */
object TimeZoneSerializer : KSerializer<TimeZone> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("kotlinx.datetime.TimeZone", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TimeZone) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: Decoder): TimeZone =
        TimeZone.of(decoder.decodeString())
}
