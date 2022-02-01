package com.veosps.game.event.impl

import com.veosps.game.event.Event
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.ui.Component
import com.veosps.game.models.ui.UserInterface

data class OpenTopLevel(
    val player: Player,
    val top: UserInterface
) : Event

data class CloseTopLevel(
    val player: Player,
    val top: UserInterface
) : Event

data class OpenModal(
    val player: Player,
    val parent: Component,
    val modal: UserInterface
) : Event

data class CloseModal(
    val player: Player,
    val parent: Component,
    val modal: UserInterface
) : Event

data class OpenOverlay(
    val player: Player,
    val parent: Component,
    val overlay: UserInterface
) : Event

data class CloseOverlay(
    val player: Player,
    val parent: Component,
    val overlay: UserInterface
) : Event
