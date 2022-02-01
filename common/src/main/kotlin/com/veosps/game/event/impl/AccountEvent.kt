package com.veosps.game.event.impl

import com.veosps.game.event.Event
import com.veosps.game.models.Client
import com.veosps.game.models.entities.mob.Player

data class AccountCreation(
    val client: Client
) : Event {

    val player: Player
        get() = client.player
}
