package com.veosps.game.protocol.update.npc.task

import com.veosps.game.collision.CollisionMap
import com.veosps.game.models.entities.mob.Npc
import com.veosps.game.models.entities.mob.NpcList
import com.veosps.game.models.world.move.MovementSpeed
import com.veosps.game.models.world.move.angle
import com.veosps.game.protocol.update.pollSteps
import com.veosps.game.protocol.update.task.UpdateTask
import com.veosps.game.protocol.update.speed
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class NpcPreUpdateTask(
    private val npcList: NpcList,
    private val collision: CollisionMap
) : UpdateTask {

    override suspend fun execute() {
        npcList.forEach { npc ->
            if (npc == null) {
                return@forEach
            }
            npc.processMovement()
        }
    }

    private fun Npc.processMovement() {
        pollSteps()
        if (!displace && movement.nextSteps.isNotEmpty()) {
            updateMovementSpeed()
        }
    }

    private fun Npc.pollSteps() {
        if (movement.isEmpty()) return
        movement.pollSteps(coordinates, speed(), collision)
        val lastStep = movement.nextSteps.lastOrNull() ?: return
        coordinates = lastStep.dest
        orientation = lastStep.dir.angle
    }

    private fun Npc.updateMovementSpeed() {
        val movementSpeed = if (movement.nextSteps.size <= 1) MovementSpeed.Walk else MovementSpeed.Run
        if (movementSpeed != lastSpeed) {
            lastSpeed = movementSpeed
        }
    }
}
