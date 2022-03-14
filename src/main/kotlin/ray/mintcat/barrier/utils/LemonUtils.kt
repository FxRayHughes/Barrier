package ray.mintcat.barrier.utils

import kotlinx.serialization.ExperimentalSerializationApi
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import ray.mintcat.barrier.Barrier
import ray.mintcat.barrier.common.BarrierPoly
import ray.mintcat.barrier.common.permission.Permission
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.common5.Baffle
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.type.Basic
import taboolib.platform.util.buildItem
import java.text.SimpleDateFormat
import java.util.*


fun Location.getPoly(): BarrierPoly? {
    return Barrier.polys.firstOrNull { it.inNode(this) }
}

fun Permission.register() {
    if (!Barrier.permissions.map { it.id }.contains(this.id)) {
        Barrier.permissions.add(this)
    }
}

fun String.asPapi(player: Player): String {
    return PlaceholderAPI.setPlaceholders(player, this);
}

fun Collection<String>.asPapi(player: Player): List<String> {
    return this.map { it.asPapi(player) }
}

fun String.asChar(): Char {
    return this.toCharArray()[0]
}

fun Int.asChar(): Char {
    return this.toString().toCharArray()[0]
}

fun Long.toTimeString(): String {
    val sdf2 = SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒")
    return sdf2.format(Date(this))
}

val tpMap = HashMap<UUID, Location>()

//延迟传送 单位s
fun Player.tpDelay(mint: Int, locationTo: Location) {
    tpMap[this.uniqueId] = this.location
    this.info("${mint}s 后开始传送 请勿移动!")
    submit(delay = mint.toLong() * 20) {
        val a = this@tpDelay.location
        val b = tpMap[this@tpDelay.uniqueId] ?: return@submit
        if (a.x != b.x || a.y != b.y || a.z != b.z) {
            this@tpDelay.error("由于您的移动已取消传送!")
            tpMap.remove(this@tpDelay.uniqueId)
            return@submit
        }
        this@tpDelay.teleport(locationTo)
        tpMap.remove(this@tpDelay.uniqueId)
    }
}

fun fromLocation(location: Location): String {
    return "${location.world?.name},${location.x},${location.y},${location.z}".replace(".", "__")
}

fun toLocation(source: String): Location {
    return source.replace("__", ".").split(",").run {
        Location(
            Bukkit.getWorld(get(0)),
            getOrElse(1) { "0" }.asDouble(),
            getOrElse(2) { "0" }.asDouble(),
            getOrElse(3) { "0" }.asDouble()
        )
    }
}

fun String.asDouble(): Double {
    return NumberConversions.toDouble(this)
}

val Boolean.display: String
    get() = if (this) "§a允许" else "§c阻止"

/**
 * 集合拆箱操作
 *
 * @receiver 目标集合
 * @return 目标集合里的所有集合包含的元素
 */

fun <T> Collection<Collection<T>>.devanning(): Collection<T> {
    val list = mutableListOf<T>()
    this@devanning.asSequence().map { a ->
        a.map { list.add(it) }
    }
    return list
}

fun <T> Collection<T>.contains(element: Collection<T>): Boolean {
    element.forEach {
        if (this.contains(it)) {
            return true
        }
    }
    return false
}

/**
 * 判断坐标是否再两个点形成的矩形内
 *
 * @receiver 应判断的坐标
 * @param posA 向量点A
 * @param posB 向量点B
 * @return 在范围内为true 反之为false
 * @since 1.0
 */
fun Location.isInAABB(posA: Location, posB: Location): Boolean {
    val pA = Vector(posA.x.coerceAtLeast(posB.x), posA.y.coerceAtLeast(posB.y), posA.z.coerceAtLeast(posB.z))
    val pB = Vector(posA.x.coerceAtMost(posB.x), posA.y.coerceAtMost(posB.y), posA.z.coerceAtMost(posB.z))
    return this.toVector().isInAABB(pB, pA)
}

/**
 * 给目标玩家发送一些消息 (提示)
 *
 * @receiver 目标玩家
 * @param block 发送的内容 可包含 & 会自动替换为
 * @since 1.0
 */
fun Player.info(vararg block: String) {
    block.forEach {
        toInfo(this, it)
    }
}

fun Player.infoTitle(info: String, sub: String) {
    this.sendTitle(info.replace("&", "§"), sub.replace("&", "§"), 10, 25, 10)
}

/**
 * 给目标玩家发送一些消息 (警告)
 *
 * @receiver 目标玩家
 * @param block 发送的内容 可包含 & 会自动替换为
 * @since 1.0
 */
fun Player.error(vararg block: String) {
    block.forEach {
        toError(this, it)
    }
}

fun Basic.set(c: Char, buildItem: ItemStack, function: (event: ClickEvent) -> Unit) {
    set(c, buildItem(buildItem) {
        colored()
    })
    onClick(c, function)
}

fun CommandSender.error(vararg block: String) {
    block.forEach {
        toError(this, it)
    }
}

fun CommandSender.info(vararg block: String) {
    block.forEach {
        toInfo(this, it)
    }
}

/**
 * 给管理者发送DEBUG信息
 * 给管理者 Ray_Hughes BingZi233
 *
 * @param block 发送的内容 可包含 & 会自动替换为
 * @since 1.0
 */
fun debug(vararg block: Any) {
    if (Bukkit.getPlayerExact("Ray_Hughes") != null) {
        block.forEach {
            toError(Bukkit.getPlayerExact("Ray_Hughes")!!, it.toString())
        }
    }
}

/**
 * 发送信息
 *
 * @param sender 接收者
 * @param message 信息
 * @since 1.0
 */
fun toInfo(sender: CommandSender, message: String) {
    sender.sendMessage("§8[§a Barrier §8] §7${message.replace("&", "§")}")
    if (sender is Player && !cooldown.hasNext(sender.name)) {
        sender.playSound(sender.location, Sound.UI_BUTTON_CLICK, 1f, (1..2).random().toFloat())
    }
}

/**
 * 发送信息
 *
 * @param sender 接收者
 * @param message 信息
 * @since 1.0
 */
fun toError(sender: CommandSender, message: String) {
    sender.sendMessage("§8[§4 Barrier §8] §7${message.replace("&", "§")}")
    if (sender is Player && !cooldown.hasNext(sender.name)) {
        sender.playSound(sender.location, Sound.ENTITY_VILLAGER_NO, 1f, (1..2).random().toFloat())
    }
}

/**
 * 发送信息
 *
 * @param sender 接收者
 * @param message 信息
 * @since 1.0
 */
fun toDone(sender: CommandSender, message: String) {
    sender.sendMessage("§8[§6 Barrier §8] §7${message.replace("&", "§")}")
    if (sender is Player && !cooldown.hasNext(sender.name)) {
        sender.playSound(sender.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, (1..2).random().toFloat())
    }
}

/**
 * 发送信息到后台
 *
 * @param message 信息
 * @since 1.0
 */
fun toConsole(message: String) {
    Bukkit.getConsoleSender().sendMessage("§8[§e Barrier §8] §7${message.replace("&", "§")}")
}

/**
 * 音效的一个CD 防止噪音
 */
val cooldown = Baffle.of(100)

fun List<String>.eval(player: Player) {
    try {
        KetherShell.eval(this, sender = adaptPlayer(player))
    } catch (e: Throwable) {
        e.printKetherErrorMessage()
    }
}

infix fun String.papi(player: Player): String {
    return PlaceholderAPI.setPlaceholders(player, this);
}