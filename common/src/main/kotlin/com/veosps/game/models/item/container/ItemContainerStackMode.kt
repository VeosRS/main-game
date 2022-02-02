package com.veosps.game.models.item.container

import com.veosps.game.cache.types.item.ItemType

sealed class ItemContainerStackMode {
    object Default : ItemContainerStackMode()
    object Always : ItemContainerStackMode()
    object Never : ItemContainerStackMode()
}

internal fun ItemContainerStackMode.stacks(type: ItemType): Boolean = when (this) {
    ItemContainerStackMode.Never -> false
    ItemContainerStackMode.Always -> true
    ItemContainerStackMode.Default -> type.canStack
}
