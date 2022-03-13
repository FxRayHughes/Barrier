package ray.mintcat.barrier.common

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import ray.mintcat.barrier.Barrier
import ray.mintcat.barrier.utils.PolyUtils
import ray.mintcat.barrier.utils.PolyUtils.getLineSegment
import ray.mintcat.barrier.utils.debug
import ray.mintcat.barrier.utils.serializable.LocationSerializer
import ray.mintcat.barrier.utils.serializable.UUIDSerializable
import ray.mintcat.barrier.utils.tpDelay
import java.util.*


@Serializable
class BarrierPoly(
    var name: String,
    @Serializable(with = UUIDSerializable::class)
    var admin: UUID,
    @Serializable(with = LocationSerializer::class)
    var door: Location,
    val nodes: MutableList<@Serializable(with = LocationSerializer::class) Location> = mutableListOf(),
    val permissions: MutableMap<String, Boolean> = mutableMapOf(),
    val users: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf()
) {

    fun teleport(player: Player) {
        if (player.isOp) {
            player.teleport(door)
        } else {
            player.tpDelay(3, door)
        }
    }

    fun hasPermission(key: String, player: String? = null, def: Boolean? = false): Boolean {
        return if (Bukkit.getPlayerExact(player ?: "null")?.isOp == true || users[player]?.get("admin") == true) {
            return true
        } else if (player != null && users.containsKey(player)) {
            if (users[player]!![key] == null) {
                permissions[key] ?: Barrier.permissions.firstOrNull { it.id == key }?.default ?: def!!
            } else {
                users[player]!![key]
            }
            Barrier.permissions.firstOrNull { it.id == key }?.default ?: def!!
        } else {
            permissions[key] ?: Barrier.permissions.firstOrNull { it.id == key }?.default ?: def!!
        }
    }

    fun caculate(): Double {
        var i = 0
        var temp = 0.0
        while (i < nodes.size - 1) {
            temp += (nodes[i].x - nodes[i + 1].x) * (nodes[i].y + nodes[i + 1].y)
            i++
        }
        temp += (nodes[i].x - nodes[0].x) * (nodes[i].y + nodes[0].y)
        return temp / 2
    }

    fun inNode(pointR: Location): Boolean {
        val point = pointR.block.location
        if (PolyUtils.pointInPolygon(point, getLineSegment(this))) {
            return true
        } else {
            val polygon1 = BarrierPoly(name, admin, door, mutableListOf(point))
            if (!PolyUtils.fastExclude(polygon1, this)) {
                return false
            }
            return true
        }
    }

}