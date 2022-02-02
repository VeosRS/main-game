package com.veosps.game.protocol.update

import com.veosps.game.collision.CollisionMap
import com.veosps.game.collision.canTraverse
import com.veosps.game.models.entities.mob.Mob
import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.world.move.Direction
import com.veosps.game.models.world.move.MovementQueue
import com.veosps.game.models.world.move.MovementSpeed
import com.veosps.game.models.world.move.Step

internal val MovementSpeed.stepCount: Int
    get() = when (this) {
        MovementSpeed.Run -> 2
        MovementSpeed.Walk -> 1
    }

internal fun MovementQueue.pollSteps(src: Coordinates, speed: MovementSpeed, collision: CollisionMap) {
    var lastCoords = src
    for (i in 0 until speed.stepCount) {
        val dest = poll() ?: break
        val dir = directionBetween(lastCoords, dest)
        if (!noclip && !collision.canTraverse(lastCoords, dir)) {
            break
        }
        val step = Step(dest, dir)
        nextSteps.add(step)
        lastCoords = dest
    }
}

fun Mob.speed(): MovementSpeed {
    return movement.speed ?: speed
}

private fun directionBetween(start: Coordinates, end: Coordinates): Direction {
    val diffX = end.x - start.x
    val diffY = end.y - start.y
    return when {
        diffX > 0 && diffY > 0 -> Direction.NorthEast
        diffX > 0 && diffY == 0 -> Direction.East
        diffX > 0 && diffY < 0 -> Direction.SouthEast
        diffX < 0 && diffY > 0 -> Direction.NorthWest
        diffX < 0 && diffY == 0 -> Direction.West
        diffX < 0 && diffY < 0 -> Direction.SouthWest
        diffX == 0 && diffY > 0 -> Direction.North
        else -> Direction.South
    }
}