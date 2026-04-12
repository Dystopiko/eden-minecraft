package com.github.dystopiko.edenmc.config

import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private val definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)

object CronSerializer: KSerializer<Cron> {
    override val descriptor: SerialDescriptor
        = PrimitiveSerialDescriptor("Crontab expression", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Cron) {
        encoder.encodeString(value.asString())
    }

    override fun deserialize(decoder: Decoder): Cron
        = CronParser(definition).parse(decoder.decodeString())
}
