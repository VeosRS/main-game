package com.veosps.game.protocol.update.player.task

import com.veosps.game.collision.CollisionMap
import com.veosps.game.models.entities.PlayerEntity
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.mob.PlayerList
import com.veosps.game.models.map.*
import com.veosps.game.models.repository.XteaRepository
import com.veosps.game.models.ui.clearMinimapFlag
import com.veosps.game.models.ui.sendRunEnergy
import com.veosps.game.models.ui.updateAppearance
import com.veosps.game.models.world.move.MovementSpeed
import com.veosps.game.models.world.move.angle
import com.veosps.game.protocol.packet.server.RebuildNormal
import com.veosps.game.protocol.packet.update.MovementPermMask
import com.veosps.game.protocol.packet.update.MovementTempMask
import com.veosps.game.protocol.update.of
import com.veosps.game.protocol.update.pollSteps
import com.veosps.game.protocol.update.task.UpdateTask
import com.veosps.game.protocol.update.speed
import com.veosps.game.protocol.update.stepCount
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class PlayerPreUpdateTask(
    private val playerList: PlayerList,
    private val xteasRepository: XteaRepository,
    private val mapIsolation: MapIsolation,
    private val collision: CollisionMap
) : UpdateTask {

    override suspend fun execute() {
        playerList.forEach { player ->
            if (player == null) {
                return@forEach
            }
            player.entityUpdate()
            player.movementUpdate()
        }
    }

    private fun Player.entityUpdate(
        oldEntity: PlayerEntity = snapshot.entity,
        curEntity: PlayerEntity = entity
    ) {
        // TODO: other flags that require an appearance update
        val appearanceUpdate = oldEntity.username != curEntity.username
        if (appearanceUpdate) {
            updateAppearance()
        }
    }

    private fun Player.movementUpdate() {
        processMovement()
        val rebuild = shouldRebuildMap()
        if (rebuild) {
            val newViewport = coordinates.zone().viewport(mapIsolation)
            val rebuildNormal = RebuildNormal(
                gpi = null,
                playerZone = coordinates.zone(),
                viewport = newViewport,
                xteas = xteasRepository
            )
            viewport = Viewport.of(coordinates, newViewport)
            write(rebuildNormal)
        }
    }

    private fun Player.processMovement() {
        pollSteps()
        if (displace) {
            displace()
        } else if (movement.nextSteps.isNotEmpty()) {
            updateMovementSpeed()
        }
    }

    private fun Player.pollSteps() {
        if (movement.isEmpty()) return
        if (speed() == MovementSpeed.Run && runEnergy <= 0) {
            movement.speed = null
            speed = MovementSpeed.Walk
        }
        movement.pollSteps(coordinates, speed(), collision)
        val lastStep = movement.nextSteps.lastOrNull()
        if (lastStep == null) {
            clearMinimapFlag()
            return
        }
        coordinates = lastStep.dest
        orientation = lastStep.dir.angle
    }

    private fun Player.updateMovementSpeed() {
        val movementSpeed = if (movement.nextSteps.size <= 1) MovementSpeed.Walk else MovementSpeed.Run
        if (movementSpeed != MovementSpeed.Walk) {
            drainRunEnergy()
        }
        if (movementSpeed != lastSpeed) {
            val mask = MovementPermMask.of(movementSpeed.stepCount)
            entity.updates.add(mask)
            lastSpeed = movementSpeed
        }
    }

    private fun Player.displace() {
        val mask = MovementTempMask.of(127)
        entity.updates.add(mask)
    }

    private fun Player.drainRunEnergy() {
        // TODO: drain run energy
        sendRunEnergy()
    }

    private fun Player.shouldRebuildMap(): Boolean {
        val dx = coordinates.x - viewport.base.x
        val dy = coordinates.y - viewport.base.y
        return dx < BuildArea.REBUILD_BOUNDARY || dx >= BuildArea.SIZE - BuildArea.REBUILD_BOUNDARY ||
            dy < BuildArea.REBUILD_BOUNDARY || dy >= BuildArea.SIZE - BuildArea.REBUILD_BOUNDARY
    }
}
