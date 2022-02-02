package com.veosps.game.protocol.update

import com.veosps.game.models.entities.mob.Mob
import com.veosps.game.protocol.packet.update.DirectionMask
import com.veosps.game.protocol.packet.update.MovementPermMask
import com.veosps.game.protocol.packet.update.MovementTempMask

fun DirectionMask.Companion.of(mob: Mob, orientation: Int = mob.orientation): DirectionMask {
    return DirectionMask(orientation)
}

fun MovementTempMask.Companion.of(type: Int): MovementTempMask {
    return MovementTempMask(type)
}

fun MovementPermMask.Companion.of(type: Int): MovementPermMask {
    return MovementPermMask(type)
}
