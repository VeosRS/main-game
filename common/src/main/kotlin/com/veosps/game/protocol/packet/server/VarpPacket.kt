package com.veosps.game.protocol.packet.server

import com.veosps.game.protocol.message.ServerPacket

data class VarpSmall(
    val id: Int,
    val value: Int
) : ServerPacket

data class VarpLarge(
    val id: Int,
    val value: Int
) : ServerPacket

object ResetClientVarCache : ServerPacket
