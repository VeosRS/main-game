package com.veosps.game.protocol.packet.server

import com.veosps.game.models.map.MapSquare
import com.veosps.game.models.map.Zone
import com.veosps.game.models.repository.XteaRepository
import com.veosps.game.protocol.message.ServerPacket

data class RebuildNormal(
    val gpi: InitialPlayerInfo?,
    val playerZone: Zone,
    val viewport: List<MapSquare>,
    val xteas: XteaRepository
) : ServerPacket
