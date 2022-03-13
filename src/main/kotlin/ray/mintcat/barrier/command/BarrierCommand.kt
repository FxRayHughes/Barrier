package ray.mintcat.barrier.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.barrier.Barrier
import ray.mintcat.barrier.common.BarrierPoly
import ray.mintcat.barrier.common.openMenu
import ray.mintcat.barrier.event.BarrierListener
import ray.mintcat.barrier.utils.PolyUtils
import ray.mintcat.barrier.utils.error
import ray.mintcat.barrier.utils.getPoly
import ray.mintcat.barrier.utils.info
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import java.util.*

@CommandHeader(
    name = "barrier",
    aliases = ["bres"],
    permission = "barrier.main"
)
object BarrierCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    //bres create 测试
    @CommandBody
    val create = subCommand {
        dynamic {
            execute<Player> { sender, context, argument ->
                val name = context.argument(0)
                val nods = BarrierListener.createMap[sender.uniqueId]
                if (nods == null || nods.isEmpty()) {
                    sender.error("记录点为空 请手持 &f${Barrier.getTool().name} &7点击地面")
                    sender.error("左键记录点 右键删除上一个记录的点")
                    return@execute
                }
                if (Barrier.polys.firstOrNull { it.name == name } != null) {
                    sender.error("名称冲突!")
                    return@execute
                }
                val build = BarrierPoly(
                    name,
                    sender.uniqueId,
                    nods.random(),
                    nods
                )
                //money
                if (Barrier.polys.firstOrNull { PolyUtils.isCoincidence(build, it) } != null) {
                    sender.error("您的领地和其他领地冲突了 请重新设定领地范围")
                    return@execute
                }
                BarrierListener.createMap[sender.uniqueId] = mutableListOf()
                Barrier.polys.add(build)
                Barrier.save(build.name)
                sender.info("领地创建成功!")
            }
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, context, argument ->
            Barrier.polys.forEach {
                sender.info(it.name)
            }
        }
    }

    @CommandBody
    val edit = subCommand {
        dynamic(commit = "领地名") {
            suggestion<CommandSender> { sender, context ->
                Barrier.polys.map { it.name }
            }
            execute<Player> { sender, context, argument ->
                val poly = Barrier.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute kotlin.run {
                    sender.error("领地不存在")
                }
                poly.openMenu(sender)
            }
        }
        execute<Player> { sender, context, argument ->
            val poly = sender.location.getPoly() ?: return@execute kotlin.run {
                sender.error("您必须在一个领地内")
            }
            poly.openMenu(sender)
        }
    }

    @CommandBody
    val remove = subCommand {
        dynamic(commit = "领地名") {
            suggestion<CommandSender> { sender, context ->
                Barrier.polys.map { it.name }
            }
            execute<Player> { sender, context, argument ->
                val poly = Barrier.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute kotlin.run {
                    sender.error("领地不存在")
                }
                Barrier.polys.remove(poly)
                Barrier.export()
                sender.info("成功删除 &f${context.argument(0)} ")
            }
        }
        execute<Player> { sender, context, argument ->
            val poly = sender.location.getPoly() ?: return@execute kotlin.run {
                sender.error("您必须在一个领地内")
            }
            sender.info("成功删除 &f${poly.name} ")
            Barrier.polys.remove(poly)
            Barrier.export()
        }
    }

    @CommandBody
    val tp = subCommand {
        dynamic(commit = "领地名") {
            suggestion<CommandSender> { sender, context ->
                Barrier.polys.map { it.name }
            }
            dynamic(commit = "玩家名") {
                suggestion<CommandSender> { sender, context ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                execute<CommandSender> { sender, context, argument ->
                    val name = Barrier.polys.firstOrNull { it.name == context.argument(-1) } ?: return@execute
                    val player = Bukkit.getPlayerExact(context.argument(0)) ?: return@execute
                    name.teleport(player)
                }
            }
            execute<Player> { sender, context, argument ->
                val name = Barrier.polys.firstOrNull { it.name == context.argument(0) } ?: return@execute
                name.teleport(sender)
            }
        }
    }
}