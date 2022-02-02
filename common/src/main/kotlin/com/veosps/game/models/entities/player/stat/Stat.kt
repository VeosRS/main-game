package com.veosps.game.models.entities.player.stat

data class Stat(var currLevel: Int, var experience: Double) {

    companion object {

        val ZERO = Stat(0, 0.0)
    }
}

fun Stat.baseLevel(): Int {
    return Stats.levelForExp(experience)
}

fun Stat.hasBaseLevel(minLevel: Int): Boolean {
    return baseLevel() >= minLevel
}

fun Stat.hasCurrLevel(minLevel: Int): Boolean {
    return currLevel >= minLevel
}
