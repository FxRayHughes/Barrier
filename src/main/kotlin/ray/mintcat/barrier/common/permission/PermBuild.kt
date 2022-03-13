package ray.mintcat.barrier.common.permission

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
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


object PermBuild : Permission, Listener {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "build"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.BRICKS) {
            name = "&f建筑 ${value.display} &7($id)"
            lore.addAll(
                listOf(
                    "",
                    "&7允许行为:",
                    "&8放置方块, 破坏方块, 放置挂饰, 破坏挂饰",
                    "&8放置盔甲架, 破坏盔甲架, 装满桶, 倒空桶"
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
    fun e(e: BlockBreakEvent) {
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.error("缺少权限 &f$id")
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: BlockPlaceEvent) {
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.error("缺少权限 &f$id")
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: HangingPlaceEvent) {
        val player = e.player ?: return
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", player.name)) {
                e.isCancelled = true
                player.error("缺少权限 &f$id")
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: HangingBreakByEntityEvent) {
        if (e.remover is Player) {
            val player = e.remover as Player
            e.entity.location.block.location.getPoly()?.run {
                if (!hasPermission("build", player.name)) {
                    e.isCancelled = true
                    player.error("缺少权限 &f$id")
                }
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.item?.type == org.bukkit.Material.ARMOR_STAND) {
            e.clickedBlock?.location?.getPoly()?.run {
                if (!hasPermission("build", e.player.name)) {
                    e.isCancelled = true
                    e.player.error("缺少权限 &f$id")
                }
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: EntityDamageByEntityEvent) {
        if (e.entity is ArmorStand) {
            val player = e.damager as? Player ?: return
            e.entity.location.block.location.getPoly()?.run {
                if (!hasPermission("build", player.name)) {
                    e.isCancelled = true
                    player.error("缺少权限 &f$id")
                }
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerBucketFillEvent) {
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.error("缺少权限 &f$id")
            }
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun e(e: PlayerBucketEmptyEvent) {
        e.block.location.getPoly()?.run {
            if (!hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.error("缺少权限 &f$id")
            }
        }
    }
}