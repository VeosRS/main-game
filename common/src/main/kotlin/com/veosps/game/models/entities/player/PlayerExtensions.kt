package com.veosps.game.models.ui

import com.veosps.game.cache.types.ui.ComponentType
import com.veosps.game.cache.types.ui.InterfaceType
import com.veosps.game.event.impl.OpenOverlay
import com.veosps.game.event.impl.OpenTopLevel
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.player.MessageType
import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.ui.gameframe.GameFrame
import com.veosps.game.models.vars.setVarp
import com.veosps.game.models.vars.type.VarpType
import com.veosps.game.protocol.packet.server.*
import com.veosps.game.protocol.packet.update.AppearanceMask
import com.veosps.game.protocol.update.player.mask.of

fun Player.openGameFrame(frame: GameFrame) {
    openTopLevel(frame.topLevel)
    frame.components.values.forEach { component ->
        openOverlay(component.inter, component.target)
    }
}

fun Player.openTopLevel(userInterface: UserInterface) {
    if (ui.topLevel.contains(userInterface)) {
        warn { "Interface list already contains top-level interface (ui=$userInterface)" }
        return
    }
    val event = OpenTopLevel(this, userInterface)
    ui.topLevel.add(userInterface)
    submitEvent(event)
    write(IfOpenTop(userInterface.id))
}

fun Player.openTopLevel(type: InterfaceType) {
    return openTopLevel(type.toUserInterface())
}

fun Player.openOverlay(
    overlay: UserInterface,
    target: Component,
    clickMode: InterfaceClickMode = InterfaceClickMode.Enabled
) {
    if (ui.overlays.contains(target)) {
        warn { "Interface list already contains overlay in target component (target=$target)" }
        return
    }
    val event = OpenOverlay(this, target, overlay)
    ui.overlays[target] = overlay
    submitEvent(event)
    write(IfOpenSub(overlay.id, target.packed, clickMode.id))
}

fun Player.openOverlay(
    overlayType: InterfaceType,
    targetType: ComponentType,
    clickMode: InterfaceClickMode = InterfaceClickMode.Enabled
) {
    return openOverlay(overlayType.toUserInterface(), targetType.toComponent(), clickMode)
}

private val InterfaceClickMode.id: Int
    get() = when (this) {
        InterfaceClickMode.Disabled -> 0
        InterfaceClickMode.Enabled -> 1
    }

fun Player.sendMessage(text: String, type: Int = MessageType.GAME_MESSAGE, username: String? = null) {
    write(MessageGame(type, text, username))
}

fun Player.setVarp(type: VarpType, value: Int) = varpMap.setVarp(type, value)

fun Player.setVarp(type: VarpType, flag: Boolean, falseValue: Int = 0, trueValue: Int = 1) {
    return varpMap.setVarp(type, flag, falseValue, trueValue)
}

fun Player.runClientScript(id: Int, vararg args: Any) {
    write(RunClientScript(id, *args))
}

fun Player.sendRunEnergy(energy: Int = runEnergy.toInt()) {
    write(UpdateRunEnergy(energy))
}

fun Player.updateAppearance() {
    val mask = AppearanceMask.of(this)
    entity.updates.add(mask)
}

fun Player.sendMinimapFlag(coords: Coordinates) {
    sendMinimapFlag(coords.x, coords.y)
}

fun Player.sendMinimapFlag(x: Int, y: Int) {
    val base = viewport.base
    val lx = (x - base.x)
    val ly = (y - base.y)
    write(MinimapFlagSet(lx, ly))
}

fun Player.clearMinimapFlag() {
    sendMinimapFlag(-1, -1)
}

fun Player.sendVarp(varp: Int, value: Int) {
    val packet = when (value) {
        in Byte.MIN_VALUE..Byte.MAX_VALUE -> VarpSmall(varp, value)
        else -> VarpLarge(varp, value)
    }
    write(packet)
}