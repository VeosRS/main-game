package com.veosps.game.models.world.move

import com.veosps.game.models.map.Coordinates

data class MoveRequest(
    val tempSpeed: MovementSpeed?,
    val stopPreviousMovement: Boolean,
    val cannotReachMessage: String?,
    val reachAction: () -> Unit,
    val buildRoute: () -> MoveRoute
)

data class MoveRoute(
    private val coords: List<Coordinates>,
    val failed: Boolean,
    val alternative: Boolean
) : List<Coordinates> by coords
