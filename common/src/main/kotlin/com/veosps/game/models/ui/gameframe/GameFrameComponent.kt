package com.veosps.game.models.ui.gameframe

import com.veosps.game.cache.types.ui.ComponentType
import com.veosps.game.cache.types.ui.InterfaceType

data class GameFrameComponent(val name: String, val inter: InterfaceType, val target: ComponentType)

data class GameFrameNameComponent(val name: String, val inter: String, val target: String)

class GameFrameComponentMap(
    private val components: LinkedHashMap<String, GameFrameComponent>
) : Map<String, GameFrameComponent> by components {

    fun add(component: GameFrameComponent) {
        components[component.name] = component
    }
}
