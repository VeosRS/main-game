package com.veosps.game.event.impl

import com.veosps.game.event.Event
import com.veosps.game.event.timer.TimerKey
import com.veosps.game.models.entities.mob.Npc
import com.veosps.game.models.entities.mob.Player

data class PlayerTimerTrigger(
    val player: Player,
    val key: TimerKey
) : Event

data class NpcTimerTrigger(
    val npc: Npc,
    val key: TimerKey
) : Event
