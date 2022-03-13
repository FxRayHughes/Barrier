package ray.mintcat.barrier.common.permission

import kotlinx.serialization.ExperimentalSerializationApi
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import ray.mintcat.barrier.utils.display
import ray.mintcat.barrier.utils.register
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

@ExperimentalSerializationApi
object PermAdmin : Permission {

    @Awake(LifeCycle.ENABLE)
    private fun init() {
        register()
    }

    override val id: String
        get() = "admin"

    override val priority: Int
        get() = -1

    override val worldSide: Boolean
        get() = false

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return buildItem(XMaterial.COMMAND_BLOCK) {
            name = "&f最高权力 ${value.display} &7($id)"
            lore.addAll(
                listOf(
                    "",
                    "&7允许行为:",
                    "&8破坏领域, 扩展领域, 管理领域",
                    "",
                    "&4注意!",
                    "&c对方将获得你的所有权力"
                )
            )
            flags.addAll(ItemFlag.values())
            if (value) {
                shiny()
            }
            colored()
        }
    }
}