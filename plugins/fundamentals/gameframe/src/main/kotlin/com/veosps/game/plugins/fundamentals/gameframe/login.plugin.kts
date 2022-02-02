package com.veosps.game.plugins.fundamentals.gameframe

import com.veosps.game.config.models.GameConfig
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.player.MessageType
import com.veosps.game.models.ui.*
import com.veosps.game.models.ui.gameframe.GameFrameFixed
import com.veosps.game.models.ui.gameframe.GameFrameList
import com.veosps.game.plugin.onEarlyLogin
import com.veosps.game.plugin.onLogin
import com.veosps.game.protocol.packet.server.ResetAnims
import com.veosps.game.protocol.packet.server.ResetClientVarCache

val config: GameConfig by inject()
val frames: GameFrameList by inject()

val varp1 = varp("varp_1055")
val varp2 = varp("varp_1737")

onLogin {
    player.sendLogin()
}

onEarlyLogin {
    val fixedFrame = frames.getValue(GameFrameFixed)
    player.openGameFrame(fixedFrame)
    player.sendEarlyLogin()
}

fun Player.sendLogin() {
    sendMessage("Welcome to ${config.name}.", MessageType.WELCOME, this.username)
    write(ResetClientVarCache)
    write(ResetAnims)
}

fun Player.sendEarlyLogin() {
    setVarp(varp1, 0)
    setVarp(varp2, -1)
    runClientScript(1105, 1)
    sendRunEnergy()
}
