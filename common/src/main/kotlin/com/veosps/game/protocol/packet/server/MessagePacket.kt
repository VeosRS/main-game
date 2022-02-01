package com.veosps.game.protocol.packet.server

import com.veosps.game.protocol.message.ServerPacket

data class MessageGame(
    val type: Int,
    val text: String,
    val username: String?
) : ServerPacket
