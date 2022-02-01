package com.veosps.game.protocol.message

import com.veosps.game.models.Client
import com.veosps.game.models.entities.mob.Player

interface ServerPacketListener {

    fun write(packet: ServerPacket)

    fun flush()

    fun close()
}

interface ClientPacketHandler<T : ClientPacket> {
    fun handle(client: Client, player: Player, packet: T)
}
