package com.veosps.game.models.world.move

import com.veosps.game.models.map.Coordinates
import java.util.LinkedList
import java.util.Queue

sealed class MovementSpeed {
    object Walk : MovementSpeed()
    object Run : MovementSpeed()
}

data class Step(
    val dest: Coordinates,
    val dir: Direction
)

class MovementQueue internal constructor(
    private val path: Queue<Coordinates> = LinkedList(),
    val nextSteps: MutableList<Step> = mutableListOf(),
    var speed: MovementSpeed? = null,
    var noclip: Boolean = false
) : Queue<Coordinates> by path
