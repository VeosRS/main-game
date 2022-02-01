package com.veosps.game.event.impl

import com.veosps.game.event.Event
import com.veosps.game.models.entities.mob.Player

data class LoginEvent(
    val player: Player,
    val priority: Priority
) : Event {

    sealed class Priority {
        object High : Priority()
        object Normal : Priority()
        object Low : Priority()
    }
}

data class LogoutEvent(
    val player: Player
) : Event
