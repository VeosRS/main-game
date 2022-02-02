package com.veosps.game.plugin

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.cache.types.item.ItemType
import com.veosps.game.cache.types.item.equipmentOptions
import com.veosps.game.cache.types.ui.ComponentType
import com.veosps.game.cache.types.ui.InterfaceType
import com.veosps.game.event.impl.*
import com.veosps.game.protocol.packet.ButtonClick
import com.veosps.game.protocol.packet.ItemAction

fun Plugin.onEarlyLogin(block: LoginEvent.() -> Unit) {
    onEvent<LoginEvent>()
        .where { priority == LoginEvent.Priority.High }
        .then(block)
}

fun Plugin.onLogin(block: LoginEvent.() -> Unit) {
    onEvent<LoginEvent>()
        .where { priority == LoginEvent.Priority.Normal }
        .then(block)
}

fun Plugin.onPostLogin(block: LoginEvent.() -> Unit) {
    onEvent<LoginEvent>()
        .where { priority == LoginEvent.Priority.Low }
        .then(block)
}

fun Plugin.onLogout(block: LogoutEvent.() -> Unit) {
    onEvent<LogoutEvent>().then(block)
}

// fun Plugin.onCommand(cmd: String, block: CommandBuilder.() -> Unit) {
//     commands.register(cmd, block)
// }

fun Plugin.onOpenTopLevel(top: InterfaceType, block: OpenTopLevel.() -> Unit) {
    onEvent<OpenTopLevel>()
        .where { this.top == top.toUserInterface() }
        .then(block)
}

fun Plugin.onCloseTopLevel(top: InterfaceType, block: CloseTopLevel.() -> Unit) {
    onEvent<CloseTopLevel>()
        .where { this.top == top.toUserInterface() }
        .then(block)
}

fun Plugin.onOpenModal(modal: InterfaceType, block: OpenModal.() -> Unit) {
    onEvent<OpenModal>()
        .where { this.modal == modal.toUserInterface() }
        .then(block)
}

fun Plugin.onCloseModal(modal: InterfaceType, block: CloseModal.() -> Unit) {
    onEvent<CloseModal>()
        .where { this.modal == modal.toUserInterface() }
        .then(block)
}

fun Plugin.onOpenOverlay(overlay: InterfaceType, block: OpenOverlay.() -> Unit) {
    onEvent<OpenOverlay>()
        .where { this.overlay == overlay.toUserInterface() }
        .then(block)
}

fun Plugin.onCloseOverlay(overlay: InterfaceType, block: CloseOverlay.() -> Unit) {
    onEvent<CloseOverlay>()
        .where { this.overlay == overlay.toUserInterface() }
        .then(block)
}

fun Plugin.onButton(component: ComponentType, block: ButtonClick.() -> Unit) {
    onAction(component.id, block)
}

//fun Plugin.onEquip(slot: Int, block: EquipItem.() -> Unit) {
//    onEvent<EquipItem>()
//        .where { this.slot == slot }
//        .then(block)
//}

fun Plugin.onItem(type: ItemType, opt: String, block: ItemAction.() -> Unit) {
    val invOption = type.interfaceOptions.optionIndex(opt, type.name, type.id, "item")
    if (invOption != -1) {
        when (invOption) {
            0 -> onAction<ItemAction.Inventory1>(type.id, block)
            1 -> onAction<ItemAction.Inventory2>(type.id, block)
            2 -> onAction<ItemAction.Inventory3>(type.id, block)
            3 -> onAction<ItemAction.Inventory4>(type.id, block)
            4 -> onAction<ItemAction.Inventory5>(type.id, block)
            5 -> onAction<ItemAction.ExamineAction>(type.id, block)
            else -> error("Unhandled item inventory option. (item=${type.name}, id=${type.id}, option=$invOption)")
        }
        return
    }
    val equipOption = type.equipmentOptions().optionIndex(opt, type.name, type.id, "item")
    if (equipOption != -1) {
        when (equipOption) {
            0 -> onAction<ItemAction.Equipment1>(type.id, block)
            1 -> onAction<ItemAction.Equipment2>(type.id, block)
            2 -> onAction<ItemAction.Equipment3>(type.id, block)
            3 -> onAction<ItemAction.Equipment4>(type.id, block)
            4 -> onAction<ItemAction.Equipment5>(type.id, block)
            5 -> onAction<ItemAction.Equipment6>(type.id, block)
            6 -> onAction<ItemAction.Equipment7>(type.id, block)
            7 -> onAction<ItemAction.Equipment8>(type.id, block)
            else -> error("Unhandled item equipment option. (item=${type.name}, id=${type.id}, option=$equipOption)")
        }
        return
    }
}

//fun Plugin.onNpc(type: NpcType, opt: String, block: NpcAction.() -> Unit) {
//    when (val option = type.options.optionIndex(opt, type.name, type.id, "npc")) {
//        0 -> onAction<NpcAction.Option1>(type.id, block)
//        1 -> onAction<NpcAction.Option2>(type.id, block)
//        2 -> onAction<NpcAction.Option3>(type.id, block)
//        3 -> onAction<NpcAction.Option4>(type.id, block)
//        4 -> onAction<NpcAction.Option5>(type.id, block)
//        else -> error("Unhandled npc option. (npc=${type.name}, id=${type.id}, option=$option)")
//    }
//}
//
//fun Plugin.onObj(type: ObjectType, opt: String, block: ObjectAction.() -> Unit) {
//    when (val option = type.options.optionIndex(opt, type.name, type.id, "object")) {
//        0 -> onAction<ObjectAction.Option1>(type.id, block)
//        1 -> onAction<ObjectAction.Option2>(type.id, block)
//        2 -> onAction<ObjectAction.Option3>(type.id, block)
//        3 -> onAction<ObjectAction.Option4>(type.id, block)
//        4 -> onAction<ObjectAction.Option5>(type.id, block)
//        else -> error("Unhandled object option. (obj=${type.name}, id=${type.id}, option=$option)")
//    }
//}

//fun Plugin.onItem(item: String, opt: String, block: ItemAction.() -> Unit) {
//    val type = item(item)
//    onItem(type, opt, block)
//}

//fun Plugin.onNpc(npc: String, opt: String, block: NpcAction.() -> Unit) {
//    val type = npc(npc)
//    onNpc(type, opt, block)
//}

//fun Plugin.onObj(obj: String, opt: String, block: ObjectAction.() -> Unit) {
//    val type = obj(obj)
//    onObj(type, opt, block)
//}

private fun Iterable<String?>.optionIndex(opt: String, name: String, id: Int, type: String): Int {
    val option = indexOfFirst { it != null && it.equals(opt, ignoreCase = false) }
    if (option == -1) {
        val ignoreCase = firstOrNull { it != null && it.equals(opt, ignoreCase = true) }
        if (ignoreCase != null) {
            val errorMessage = "Letter case option error for $type \"$name\" (id=$id)"
            val foundMessage = "Found [\"$ignoreCase\"] but was given [\"$opt\"]"
            error("$errorMessage. $foundMessage.")
        }
        error("Option for $type \"$name\" not found. (id=$id, option=$opt)")
    }
    return option
}
