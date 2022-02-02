package com.veosps.game.models.item

import com.google.common.base.MoreObjects
import com.veosps.game.cache.types.item.ItemType

internal const val MAX_ITEM_STACK = Int.MAX_VALUE

data class Item(
    val type: ItemType,
    val amount: Int = 1
) {

    val id: Int
        get() = type.id

    val name: String
        get() = type.name

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("id", type.id)
        .add("amount", amount)
        .toString()
}
