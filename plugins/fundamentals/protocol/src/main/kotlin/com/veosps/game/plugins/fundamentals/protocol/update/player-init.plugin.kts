package com.veosps.game.plugins.fundamentals.protocol.update

import com.veosps.game.event.impl.ClientRegister
import com.veosps.game.protocol.update.player.task.PlayerUpdateTask

val updateTask: PlayerUpdateTask by inject()

onEvent<ClientRegister>().then(::onRegister)

fun onRegister(event: ClientRegister) {
    updateTask.initClient(event.client)
}
