package com.veosps.game.cache.types.ui

import com.veosps.game.cache.types.CacheType
import com.veosps.game.models.ui.Component
import com.veosps.game.models.ui.UserInterface

data class InterfaceType(
    override val id: Int,
    val children: List<Component>
) : CacheType {

    fun toUserInterface(): UserInterface {
        return UserInterface(id)
    }
}