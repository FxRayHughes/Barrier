package ray.mintcat.barrier.event

import org.bukkit.entity.Player
import ray.mintcat.barrier.common.BarrierPoly
import taboolib.platform.type.BukkitProxyEvent

class BarrierPlayerJoinPolyEvent(
    val player: Player,
    val poly: BarrierPoly
) : BukkitProxyEvent()