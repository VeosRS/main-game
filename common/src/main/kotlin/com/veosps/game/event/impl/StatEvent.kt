package com.veosps.game.event.impl

import com.veosps.game.event.Event
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.player.stat.StatKey

data class StatLevelUp(
    val player: Player,
    val key: StatKey,
    val oldLevel: Int,
    val newLevel: Int,
    val experience: Double
) : Event
