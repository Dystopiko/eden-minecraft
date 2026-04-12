package com.github.dystopiko.edenmc.gateway

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.core.BlockPos
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

object McBlockPosSerializer: KSerializer<BlockPos> {
    private val delegate = ListSerializer(Int.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): BlockPos {
        val list = delegate.deserialize(decoder)
        if (list.size != 3) {
            throw SerializationException("BlockPos must have exactly 3 elements, got ${list.size}")
        }
        return BlockPos(list[0], list[1], list[2])
    }

    override fun serialize(encoder: Encoder, value: BlockPos) {
        delegate.serialize(encoder, listOf(value.x, value.y, value.z))
    }
}

object McUUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor
        = PrimitiveSerialDescriptor("Minecraft UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

object GatewayErrorCodeSerializer: KSerializer<GatewayErrorCode> {
    override val descriptor: SerialDescriptor
        = PrimitiveSerialDescriptor("GatewayErrorCode", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): GatewayErrorCode {
        return when (val value = decoder.decodeString()) {
            "INTERNAL" -> GatewayErrorCode.Internal
            "READONLY_MODE" -> GatewayErrorCode.ReadonlyMode
            "NOT_FOUND" -> GatewayErrorCode.NotFound
            "INVALID_REQUEST" -> GatewayErrorCode.InvalidRequest
            "SERVICE_UNAVAILABLE" -> GatewayErrorCode.ServiceUnavailable
            "RATE_LIMITED" -> GatewayErrorCode.RateLimited
            else -> GatewayErrorCode.Unknown(value)
        }
    }

    override fun serialize(encoder: Encoder, value: GatewayErrorCode) {
        encoder.encodeString(value.value)
    }
}

object Rfc3339 {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun parse(value: String): OffsetDateTime = try {
        OffsetDateTime.parse(value, formatter)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Invalid RFC 3339 timestamp: '$value'", e)
    }

    fun format(value: OffsetDateTime): String = value.format(formatter)
}

object Rfc3339Serializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Rfc3339", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): OffsetDateTime =
        Rfc3339.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: OffsetDateTime) =
        encoder.encodeString(Rfc3339.format(value))
}
