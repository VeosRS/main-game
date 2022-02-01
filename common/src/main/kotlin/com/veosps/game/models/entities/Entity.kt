package com.veosps.game.models.entities

import com.veosps.game.models.entities.player.Appearance
import com.veosps.game.protocol.update.mask.UpdateMaskSet
import com.veosps.game.models.map.Coordinates

sealed class Entity(
    var index: Int = -1,
    var coordinates: Coordinates = Coordinates.ZERO,
    val updates: UpdateMaskSet = UpdateMaskSet()
)

class PlayerEntity(
    val username: String,
    val privilege: Int,
    var appearance: Appearance = Appearance.ZERO
) : Entity() {

    fun copy() = PlayerEntity(
        username,
        privilege,
        appearance.copy()
    )

    companion object {
        val ZERO = PlayerEntity("", 0)
    }
}

class NpcEntity(
    val invisible: Boolean = false,
    val transform: Int = -1
) : Entity()
