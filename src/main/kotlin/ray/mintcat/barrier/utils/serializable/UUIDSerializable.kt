package ray.mintcat.barrier.utils.serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * ItemStackR
 * @author Ray_Hughes
 * @Time 2022/1/21
 * @since 1.0
 */
object UUIDSerializable : KSerializer<UUID> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("java.util.UUID")

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

}