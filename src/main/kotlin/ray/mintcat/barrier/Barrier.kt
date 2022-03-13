package ray.mintcat.barrier

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ray.mintcat.barrier.common.BarrierPoly
import ray.mintcat.barrier.common.permission.Permission
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.util.buildItem
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RuntimeDependencies(
    RuntimeDependency(
        value = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
    ),
    RuntimeDependency(
        value = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
    )
)
object Barrier : Plugin() {

    @Config(migrate = true, value = "settings.yml", autoReload = true)
    lateinit var config: Configuration
        private set

    val polys = ArrayList<BarrierPoly>()

    val permissions = ArrayList<Permission>()

    //第一个是玩家ID 第二个是领地ID
    val looker = HashMap<UUID, UUID>()

    fun getTool(): Material {
        return Material.valueOf(config.getString("ClaimTool", "APPLE")!!)
    }

    private val json = Json {
        coerceInputValues = true
    }

    @Awake(LifeCycle.DISABLE)
    @Schedule(period = 20 * 60)
    fun export() {
        polys.forEach { poly ->
            save(poly.name)
        }
    }

    fun save(id: String) {
        val poly = polys.firstOrNull { it.name == id } ?: return
        newFile(
            getDataFolder(),
            "data/${id}.json"
        ).writeText(json.encodeToString(poly), StandardCharsets.UTF_8)
    }

    @Suppress("UNCHECKED_CAST")
    @Awake(LifeCycle.ACTIVE)
    fun import() {
        polys.clear()
        newFile(getDataFolder(), "data", create = false, folder = true).listFiles()?.map { file ->
            if (file.name.endsWith(".json")) {
                polys.add(json.decodeFromString(BarrierPoly.serializer(), file.readText(StandardCharsets.UTF_8)))
            }
        }
    }

}