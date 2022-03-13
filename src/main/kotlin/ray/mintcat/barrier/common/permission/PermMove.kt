package ray.mintcat.barrier.common.permission

import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
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


object PermMove : Permission, Listener {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "move"

    override val default: Boolean
        get() = true

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.IRON_BOOTS) {
            name = "&f移动 ${value.display} &7($id)"
            lore.addAll(
                listOf(
                    "",
                    "&7允许行为:",
                    "&8领域内移动"
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
    fun e(e: PlayerMoveEvent) {
        e.player.location.getPoly()?.run {
            if (!hasPermission("move", e.player.name)) {
                e.isCancelled = true
                e.player.error("缺少权限 &f$id")
                e.setTo(e.from.block.location)
            }
        }
    }
}