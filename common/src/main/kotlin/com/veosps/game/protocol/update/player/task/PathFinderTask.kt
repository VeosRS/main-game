package com.veosps.game.protocol.update.player.task

import com.veosps.game.coroutines.delay
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.mob.PlayerList
import com.veosps.game.models.ui.clearMinimapFlag
import com.veosps.game.models.ui.sendMessage
import com.veosps.game.models.ui.sendMinimapFlag
import com.veosps.game.models.world.move.MoveRequest
import com.veosps.game.protocol.update.task.UpdateTask
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class PathFinderTask(
    private val playerList: PlayerList
) : UpdateTask {

    override suspend fun execute() {
        playerList.forEach { player ->
            val request = player?.moveRequest ?: return@forEach
            player.handleMoveRequest(request)
        }
    }

    private fun Player.handleMoveRequest(request: MoveRequest) {
        if (request.stopPreviousMovement) {
            stopMovement()
        }
        clearQueues()
        val route = request.buildRoute()
        if (route.isEmpty()) {
            clearMinimapFlag()
            if (route.failed) {
                request.cannotReachMessage?.let { sendMessage(it) }
            } else if (!route.alternative) {
                request.reachAction()
            }
            return
        }
        movement.speed = request.tempSpeed
        movement.addAll(route)
        val dest = route.last()
        sendMinimapFlag(dest)
        normalQueue {
            delay()
            var reached = false
            while (!reached) {
                if (coordinates == dest) {
                    reached = true
                    break
                }
                delay()
            }
            if (!reached || route.alternative) {
                request.cannotReachMessage?.let { sendMessage(it) }
                return@normalQueue
            }
            request.reachAction()
        }
    }
}
