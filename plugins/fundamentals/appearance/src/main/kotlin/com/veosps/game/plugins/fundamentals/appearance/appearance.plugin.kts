package com.veosps.game.plugins.fundamentals.appearance

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.mob.faceDirection
import com.veosps.game.models.entities.player.appearance.Appearance
import com.veosps.game.models.ui.updateAppearance
import com.veosps.game.models.world.move.Direction
import com.veosps.game.plugin.onEarlyLogin

val logger = InlineLogger()

onEarlyLogin {
    player.setAndUpdateAppearance()
    player.faceDirection(Direction.South)
}

fun Player.setAndUpdateAppearance() {
    logger.debug { "Setting appearance for entity: ${entity.username}" }
    if (entity.appearance === Appearance.ZERO) {
        entity.appearance = AppearanceConstants.DEFAULT_APPEARANCE
    }
    updateAppearance()
}
