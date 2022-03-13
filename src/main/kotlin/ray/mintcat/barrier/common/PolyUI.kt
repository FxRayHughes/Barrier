package ray.mintcat.barrier.common

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ray.mintcat.barrier.Barrier
import ray.mintcat.barrier.common.permission.Permission
import ray.mintcat.barrier.utils.error
import ray.mintcat.barrier.utils.info
import ray.mintcat.barrier.utils.set
import taboolib.common.platform.function.submit
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.inputSign
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.inventoryCenterSlots

fun BarrierPoly.openMenu(player: Player) {
    val data = this
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    player.openMenu<Basic>("管理页面") {
        map(
            "#########",
            "#A#B#C#D#",
            "#########"
        )
        set('A', buildItem(XMaterial.ITEM_FRAME) {
            name = "&f${data.name}"
            lore.add("&7持有者:&f ${Bukkit.getOfflinePlayer(admin).name}")
            lore.add("&7唯一编号:&f ${data.name}")
            colored()
        })
        set('B', buildItem(XMaterial.COMMAND_BLOCK_MINECART) {
            name = "&f全局权限管理"
            colored()
        })
        onClick('B') {
            openPermissionMenu(player)
        }
        set('C', buildItem(XMaterial.WRITABLE_BOOK) {
            name = "&f私有权限管理"
            colored()
        })
        onClick('C') {
            openPermissionUserMenu(player)
        }
        set('D', buildItem(XMaterial.OBSERVER) {
            name = "&f领地设置"
            colored()
        })
        onClick('D') {
            openSettingMenu(player)
        }
    }
}

fun BarrierPoly.openSettingMenu(player: Player) {
    val data = this
    player.openMenu<Basic>("${data.name}设置") {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        map(
            "#########",
            "#ABCDEFG#",
            "#<#######"
        )
        set('A', buildItem(Material.ENDER_EYE) {
            name = "&f设置传送点到当前位置"
            colored()
        }) {
            data.door = player.location
            Barrier.save(data.name)
        }
    }
}

fun BarrierPoly.openPermissionUserMenu(player: Player) {
    val data = this
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    player.openMenu<Linked<String>>("${data.name}私有权限管理") {
        rows(6)
        slots(inventoryCenterSlots)
        elements {
            users.keys.filter { it != player.name }.toList()
        }
        onGenerate { _, element, _, _ ->
            if (hasPermission("admin", element)) {
                buildItem(XMaterial.PLAYER_HEAD) {
                    name = "&c管理员 $element"
                    lore.addAll(listOf(" &7- &fall", " "))
                    lore.add("&7点击修改权限")
                    skullOwner = element
                    colored()
                }
            } else {
                buildItem(XMaterial.PLAYER_HEAD) {
                    name = "&c用户 $element"
                    lore.addAll(users[element]!!.filter { it.value }.keys.map { " &7- &f${it}" })
                    lore.add(" ")
                    lore.add("&7点击修改权限")
                    skullOwner = element
                    colored()
                }
            }
        }
        onClick { _, element ->
            openPermissionUser(player, element)
        }
        set(49, buildItem(XMaterial.WRITABLE_BOOK) {
            name = "&f添加用户"
            lore.add("&7点击从列表里添加用户")
            colored()
        }) {
            openAddUserMenu(player)
        }
        setNextPage(51) { _, hasNextPage ->
            if (hasNextPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f下一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7下一页"
                }
            }
        }
        setPreviousPage(47) { _, hasPreviousPage ->
            if (hasPreviousPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f上一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7上一页"
                }
            }
        }
    }
}

fun BarrierPoly.openAddUserMenu(player: Player) {
    val data = this
    player.openMenu<Linked<Player>>("点击要添加的头像") {
        rows(6)
        slots(inventoryCenterSlots)
        elements {
            Bukkit.getOnlinePlayers().filter { it.name != player.name || !users.keys.contains(it.name) }.toList()
        }
        onGenerate { _, element, _, _ ->
            buildItem(XMaterial.PLAYER_HEAD) {
                name = "&c用户 $${element.name}"
                lore.add("&7点击添加")
                skullOwner = element.name
                colored()
            }
        }
        onClick { _: ClickEvent, element: Player ->
            users[element.name] = HashMap()
            Barrier.save(data.name)
            player.info("添加成功!")
            player.closeInventory()
            submit(delay = 1) {
                openPermissionUserMenu(player)
            }
        }
        setNextPage(51) { _, hasNextPage ->
            if (hasNextPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f下一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7下一页"
                }
            }
        }
        setPreviousPage(47) { _, hasPreviousPage ->
            if (hasPreviousPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f上一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7上一页"
                }
            }
        }

    }
}

fun BarrierPoly.openPermissionUser(player: Player, user: String) {
    val data = this
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    player.openMenu<Linked<Permission>>("$user 的权限设置") {
        rows(6)
        slots(inventoryCenterSlots)
        elements {
            val list = Barrier.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
            list.toList().forEach {
                if (it.adminSide && !player.isOp) {
                    list.remove(it)
                }
            }
            list
        }
        onGenerate { player, element, index, slot ->
            element.generateMenuItem(hasPermission(element.id, player = user, def = element.default))
        }
        set(49, buildItem(XMaterial.LAVA_BUCKET) {
            name = "&4删除用户"
            lore.add("&c将该用户从当前领地中移除")
            colored()
        }) {
            player.info("已删除 &f${user} 的所有权限!")
            users.remove(user)
            Barrier.save(data.name)
            submit(delay = 1) {
                openPermissionUserMenu(player)
            }
        }
        onClick { event, element ->
            users[user]!![element.id] = !hasPermission(element.id, player = user, def = element.default)
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
            Barrier.save(data.name)
            player.info("已修改 &f${user} &7的 &f${element.id} &7权限!")
            submit(delay = 1) {
                openPermissionUser(player, user)
            }
        }
    }
}

fun BarrierPoly.openPermissionMenu(player: Player) {
    val data = this
    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
    player.openMenu<Linked<Permission>>("${name}全局权限管理") {
        rows(6)
        slots(inventoryCenterSlots)
        elements {
            val list = Barrier.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
            if (!player.isOp) {
                list.removeAll(list.filter { it.adminSide == player.isOp })
            }
            list
        }
        onGenerate { _, element, _, _ ->
            if (element.adminSide && !player.isOp) {
                ItemStack(Material.BARRIER)
            }
            element.generateMenuItem(hasPermission(element.id, def = element.default))
        }
        onClick { event, element ->
            if (element.adminSide && !player.isOp) {
                event.clicker.error("该选项无效!")
            }
            permissions[element.id] = !hasPermission(element.id, def = element.default)
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
            Barrier.save(data.name)
            openPermissionMenu(player)
        }
        setNextPage(51) { page, hasNextPage ->
            if (hasNextPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f下一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7下一页"
                }
            }
        }
        setPreviousPage(47) { page, hasPreviousPage ->
            if (hasPreviousPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) {
                    name = "§f上一页"
                }
            } else {
                buildItem(XMaterial.ARROW) {
                    name = "§7上一页"
                }
            }
        }
    }
}