package ray.mintcat.barrier.utils.serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Location
import ray.mintcat.barrier.utils.fromLocation
import ray.mintcat.barrier.utils.toLocation

object LocationSerializer : KSerializer<Location> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Location", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Location) {
        encoder.encodeString(fromLocation(value))
    }

    override fun deserialize(decoder: Decoder): Location {
        val string = decoder.decodeString()
        return toLocation(string)
    }
}