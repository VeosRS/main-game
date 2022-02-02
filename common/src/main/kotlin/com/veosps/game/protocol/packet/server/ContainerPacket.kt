package com.veosps.game.protocol.packet.server

import com.veosps.game.models.item.Item
import com.veosps.game.protocol.message.ServerPacket

data class UpdateInvFull(
    val key: Int = -1,
    val component: Int = -1,
    val items: List<Item?>
) : ServerPacket

data class UpdateInvPartial(
    val key: Int = -1,
    val component: Int = -1,
    val updated: Map<Int, Item?>
) : ServerPacket
