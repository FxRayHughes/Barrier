package ray.mintcat.barrier.common.permission

import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import ray.mintcat.barrier.utils.display
import ray.mintcat.barrier.utils.error
import ray.mintcat.barrier.utils.getPoly
import ray.mintcat.barrier.utils.register
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem


object PermTeleport : Permission, Listener {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "teleport"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.ENDER_PEARL) {
            name = "&f传送 ${value.display} &7($id)"
            lore.addAll(
                listOf(
                    "",
                    "&7允许行为:",
                    "&8通过传送进入或离开"
                )
            )
            flags.addAll(ItemFlag.values())
            if (value) {
                shiny()
            }
            colored()
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerTeleportEvent) {
        e.from.getPoly()?.run {
            if (!hasPermission("teleport", e.player.name)) {
                e.isCancelled = true
                e.player.error("缺少权限 &f$id")
            }
        }
        e.to?.getPoly()?.run {
            if (!hasPermission("teleport", e.player.name)) {
                e.isCancelled = true
                e.player.error("缺少权限 &f$id")
            }
        }
    }
}