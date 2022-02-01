package com.veosps.game.models.vars.type

import com.veosps.game.cache.types.ConfigType
import com.veosps.game.cache.types.TypeList

data class VarpType(
    override val id: Int,
    val type: Int
) : ConfigType

class VarpTypeList : TypeList<VarpType>()
