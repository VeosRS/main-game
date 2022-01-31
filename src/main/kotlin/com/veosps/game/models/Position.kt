package com.veosps.game.models

data class Position(
    val x: Int,
    val y: Int,
    val z: Int
) {
    constructor(x: Int, y: Int) : this(x, y, 0)
}