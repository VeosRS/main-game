package com.veosps.game.models.vars.type

import com.veosps.game.cache.types.ConfigType
import com.veosps.game.cache.types.TypeList
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

data class VarpType(
    override val id: Int,
    val type: Int
) : ConfigType

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class VarpTypeList : TypeList<VarpType>()
