package com.veosps.game.models.vars.type

import com.veosps.game.cache.types.ConfigType
import com.veosps.game.cache.types.TypeList

data class VarbitType(
    override val id: Int,
    val varp: Int,
    val lsb: Int,
    val msb: Int
) : ConfigType

class VarbitTypeList : TypeList<VarbitType>()
