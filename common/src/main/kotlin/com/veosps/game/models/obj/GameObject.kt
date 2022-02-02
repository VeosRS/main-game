package com.veosps.game.models.obj

import com.google.common.base.MoreObjects
import com.veosps.game.cache.types.obj.ObjectType
import com.veosps.game.models.map.Coordinates

class GameObject(
    val type: ObjectType,
    val coords: Coordinates,
    val attributes: Int
) {

    val id: Int
        get() = type.id

    val shape: Int
        get() = attributes shr 2

    val rotation: Int
        get() = attributes and 0x3

    constructor(
        type: ObjectType,
        coords: Coordinates,
        shape: Int,
        rotation: Int
    ) : this(type, coords, (shape shl 2) or rotation)

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("shape", shape)
        .add("rot", rotation)
        .add("coords", coords)
        .toString()
}
