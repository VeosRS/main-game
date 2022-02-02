package com.veosps.game.models.vars.type

import com.veosps.game.cache.types.ConfigType
import com.veosps.game.cache.types.TypeList
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

data class VarbitType(
    override val id: Int,
    val varp: Int,
    val lsb: Int,
    val msb: Int
) : ConfigType

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class VarbitTypeList : TypeList<VarbitType>()
