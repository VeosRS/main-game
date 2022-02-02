package com.veosps.game.models.world.move

import com.veosps.game.models.map.Coordinates

private const val NEUTRAL_UNIT = 0
private const val POSITIVE_UNIT = 1
private const val NEGATIVE_UNIT = -1

sealed class Direction(val x: Int = NEUTRAL_UNIT, val y: Int = NEUTRAL_UNIT) {
    object North : Direction(y = POSITIVE_UNIT)
    object East : Direction(x = POSITIVE_UNIT)
    object South : Direction(y = NEGATIVE_UNIT)
    object West : Direction(x = NEGATIVE_UNIT)
    object NorthEast : Direction(x = POSITIVE_UNIT, y = POSITIVE_UNIT)
    object SouthEast : Direction(x = POSITIVE_UNIT, y = NEGATIVE_UNIT)
    object SouthWest : Direction(x = NEGATIVE_UNIT, y = NEGATIVE_UNIT)
    object NorthWest : Direction(x = NEGATIVE_UNIT, y = POSITIVE_UNIT)

    override fun toString(): String = javaClass.simpleName
}

fun Coordinates.translate(direction: Direction) = translate(direction.x, direction.y)

val Direction.angle: Int
    get() = when (this) {
        Direction.South -> 0
        Direction.SouthWest -> 256
        Direction.East -> 512
        Direction.NorthWest -> 768
        Direction.North -> 1024
        Direction.NorthEast -> 1280
        Direction.West -> 1536
        Direction.SouthEast -> 1792
    }
