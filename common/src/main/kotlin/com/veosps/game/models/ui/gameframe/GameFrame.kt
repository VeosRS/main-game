package com.veosps.game.models.ui.gameframe

import com.veosps.game.cache.types.ui.InterfaceType

data class GameFrame(
    val type: GameFrameType,
    val topLevel: InterfaceType,
    val components: GameFrameComponentMap
) : Map<String, GameFrameComponent> by components