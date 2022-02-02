package com.veosps.game.cache.name.vars

import com.veosps.game.models.vars.type.VarbitType
import com.veosps.game.models.vars.type.VarpType
import com.veosps.game.name.NamedTypeMap
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class VarpNameMap : NamedTypeMap<VarpType>()

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class VarbitNameMap : NamedTypeMap<VarbitType>()