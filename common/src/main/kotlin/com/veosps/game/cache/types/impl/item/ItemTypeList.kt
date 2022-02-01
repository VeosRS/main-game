package com.veosps.game.cache.types.impl.item

import com.veosps.game.cache.types.TypeList
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class ItemTypeList : TypeList<ItemType>()