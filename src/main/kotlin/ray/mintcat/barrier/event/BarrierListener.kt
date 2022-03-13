package ray.mintcat.barrier.event

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import ray.mintcat.barrier.Barrier
import ray.mintcat.barrier.utils.*
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object BarrierListener {

    val createMap = ConcurrentHashMap<UUID, MutableList<Location>>()

    @Awake(LifeCycle.ENABLE)
    fun show() {
        submit(async = true, period = 20) {
            createMap.forEach { (uuid, list) ->
                val player = Bukkit.getPlayer(uuid) ?: return@forEach
                list.forEach {
                    sendParticle(player, it)
                }
            }
        }
    }

    private val particle = ProxyParticle.END_ROD
    fun sendParticle(player: Player, location: Location) {
        particle.sendTo(
            adaptPlayer(player),
            taboolib.common.util.Location(
                location.world!!.name, location.x + 0.5, location.y + 1.5, location.z + 0.5
            ),
            count = 2
        )
    }

    @SubscribeEvent
    fun createInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (block.type == Material.AIR || event.hand == EquipmentSlot.OFF_HAND) {
            return
        }
        //物品判断
        if (event.item == null || event.item!!.type != Barrier.getTool()) {
            return
        }
        when (event.action) {
            Action.RIGHT_CLICK_BLOCK -> {
                //删除
                if (createMap[event.player.uniqueId] == null || createMap[event.player.uniqueId]!!.size == 0) {
                    event.player.error("你没有设置点")
                    event.isCancelled = true
                    return
                }
                createMap[event.player.uniqueId]!!.removeLast()
                event.player.info("删除成功!")
                event.isCancelled = true
            }
            Action.LEFT_CLICK_BLOCK -> {
                //添加
                if (createMap[event.player.uniqueId] == null || createMap[event.player.uniqueId]?.isEmpty() == true) {
                    createMap[event.player.uniqueId] = mutableListOf()
                    createMap[event.player.uniqueId]!!.add(block.location)
                    event.player.info("成功添加第 &f${createMap[event.player.uniqueId]!!.size} &7个点")
                } else {
                    if (createMap[event.player.uniqueId]!!.contains(block.location)) {
                        event.player.error("此点已包含!")
                        return
                    }
                    if (createMap[event.player.uniqueId]!!.last().world == block.world) {
                        createMap[event.player.uniqueId]!!.add(block.location)
                        event.player.info("成功添加第 &f${createMap[event.player.uniqueId]!!.size} &7个点")
                    } else {
                        event.player.error("请回到 &f${createMap[event.player.uniqueId]!!.last().world?.name}&7 世界")
                        event.isCancelled = true
                        return
                    }
                }
                event.isCancelled = true
            }
            else -> {}
        }
    }

    @SubscribeEvent
    fun join(event: PlayerMoveEvent) {
        val poly = event.to?.getPoly() ?: return
        if (event.from.getPoly() == null) {
            //视为进入一个新的领地
            BarrierPlayerJoinPolyEvent(event.player, poly).apply {
                call()
                event.isCancelled = this.isCancelled
            }
        }
    }

    @SubscribeEvent
    fun leave(event: PlayerMoveEvent) {
        if (event.from.getPoly() != null && event.to?.getPoly() == null) {
            //视为离开一个新的领地
            BarrierPlayerLeavePolyEvent(event.player, event.from.getPoly()!!).apply {
                call()
                event.isCancelled = this.isCancelled
            }
        }
    }

    @SubscribeEvent
    fun e(event: BarrierPlayerJoinPolyEvent) {
        event.player.infoTitle(
            Barrier.config.getString("Info.Join.main")!!.replace("[name]", event.poly.name),
            Barrier.config.getString("Info.Join.sub")!!.replace("[name]", event.poly.name)
        )
        Barrier.config.getStringList("Info.Join.action").eval(event.player)

    }

    @SubscribeEvent
    fun e(event: BarrierPlayerLeavePolyEvent) {
        event.player.infoTitle(
            Barrier.config.getString("Info.Leave.main")!!.replace("[name]", event.poly.name),
            Barrier.config.getString("Info.Leave.sub")!!.replace("[name]", event.poly.name)
        )
        Barrier.config.getStringList("Info.Leave.action").map { it.replace("[name]", event.poly.name) }.eval(event.player)
    }

}