package com.veosps.game.protocol.packet.client

import com.veosps.game.action.ActionBus
import com.veosps.game.models.Client
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.map.Coordinates
import com.veosps.game.protocol.message.ClientPacket
import com.veosps.game.protocol.message.ClientPacketHandler
import com.veosps.game.protocol.packet.MapMove
import com.veosps.game.protocol.packet.MoveType
import org.springframework.stereotype.Component

private const val FORCE_RUN_TYPE = 1
private const val TELE_TYPE = 2

data class MoveGameClick(
    val x: Int,
    val y: Int,
    val type: Int
) : ClientPacket

data class MoveMinimapClick(
    val x: Int,
    val y: Int,
    val type: Int
) : ClientPacket

@Component
class GameClickHandler(
    private val actions: ActionBus
) : ClientPacketHandler<MoveGameClick> {

    override fun handle(client: Client, player: Player, packet: MoveGameClick) {
        val (x, y, type) = packet
        val action = player.mapMove(x, y, type)
        actions.publish(action)
    }
}

@Component
class MinimapClickHandler(
    private val actions: ActionBus
) : ClientPacketHandler<MoveMinimapClick> {

    override fun handle(client: Client, player: Player, packet: MoveMinimapClick) {
        val (x, y, type) = packet
        val action = player.mapMove(x, y, type)
        actions.publish(action)
    }
}

private fun Player.mapMove(x: Int, y: Int, type: Int): MapMove {
    val destination = Coordinates(x, y)
    val moveType = when (type) {
        FORCE_RUN_TYPE -> MoveType.ForceRun
        TELE_TYPE -> MoveType.Displace
        else -> MoveType.Neutral
    }
    return MapMove(this, destination, moveType)
}
