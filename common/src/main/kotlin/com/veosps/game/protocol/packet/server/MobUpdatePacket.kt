package com.veosps.game.protocol.packet.server

import com.veosps.game.cache.buffer.toBitBuf
import com.veosps.game.protocol.message.ServerPacket
import io.netty.buffer.ByteBuf

class InitialPlayerInfo(
    val playerCoordsAs30Bits: Int,
    val otherPlayerCoords: IntArray
) {

    fun write(buf: ByteBuf): ByteBuf {
        val bitBuf = buf.toBitBuf()
        bitBuf.writeBits(playerCoordsAs30Bits, 30)
        otherPlayerCoords.forEach { coords ->
            bitBuf.writeBits(coords, 18)
        }
        return bitBuf.byteBuf
    }
}

class PlayerInfo(
    val buffer: ByteBuf
) : ServerPacket

class NpcInfoSmallViewport(
    val buffer: ByteBuf
) : ServerPacket

class NpcInfoLargeViewport(
    val buffer: ByteBuf
) : ServerPacket
