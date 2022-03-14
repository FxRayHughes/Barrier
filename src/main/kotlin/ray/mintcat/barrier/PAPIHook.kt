package ray.mintcat.barrier

import org.bukkit.entity.Player
import ray.mintcat.barrier.utils.getPoly
import ray.mintcat.barrier.utils.papi
import taboolib.platform.compat.PlaceholderExpansion
import java.util.*

object PAPIHook : PlaceholderExpansion {
    override val identifier: String
        get() = "barrier"

    val keySave = HashMap<UUID, String>()

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val target = player ?: return "Null"
        val info = args.apply {
            replace("{{", "%")
            replace("}}", "%")
            papi(target)
        }.split("::")
        //%lemon_chat%
        return when (info[0]) {
            "poly" -> {
                target.location.getPoly()?.name ?: info[1]
            }
            else -> {
                "Null"
            }
        }
    }
}