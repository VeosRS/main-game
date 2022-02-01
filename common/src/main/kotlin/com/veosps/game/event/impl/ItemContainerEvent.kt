package com.veosps.game.event.impl

import com.veosps.game.event.Event
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.item.container.ItemContainer
import com.veosps.game.models.item.container.ItemContainerKey

data class ItemContainerInitialize(
    val player: Player,
    val key: ItemContainerKey,
    val container: ItemContainer
) : Event

data class ItemContainerUpdate(
    val player: Player,
    val key: ItemContainerKey,
    val container: ItemContainer
) : Event
