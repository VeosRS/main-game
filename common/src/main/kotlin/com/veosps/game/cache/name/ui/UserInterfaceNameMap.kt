package com.veosps.game.cache.name.ui

import com.veosps.game.cache.types.ui.ComponentType
import com.veosps.game.cache.types.ui.InterfaceType
import com.veosps.game.name.NamedTypeMap
import org.springframework.stereotype.Component

@Component
class UserInterfaceNameMap : NamedTypeMap<InterfaceType>()

@Component
class ComponentNameMap : NamedTypeMap<ComponentType>()
