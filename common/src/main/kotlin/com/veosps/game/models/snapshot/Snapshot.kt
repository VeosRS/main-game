package com.veosps.game.models.snapshot

import com.veosps.game.models.entities.PlayerEntity
import com.veosps.game.models.entities.player.stat.StatMap
import com.veosps.game.models.item.container.ItemContainerMap
import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.vars.VarpMap
import java.time.LocalDateTime

data class Snapshot(
    val timestamp: LocalDateTime,
    val coords: Coordinates,
    val entity: PlayerEntity,
    val stats: StatMap,
    val varps: VarpMap,
    val containers: ItemContainerMap
) {

    companion object {

        val INITIAL = Snapshot(
            timestamp = LocalDateTime.now(),
            entity = PlayerEntity.ZERO,
            coords = Coordinates.ZERO,
            stats = StatMap(),
            varps = VarpMap(),
            containers = ItemContainerMap()
        )
    }
}
