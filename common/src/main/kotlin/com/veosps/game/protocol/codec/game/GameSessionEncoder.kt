package com.veosps.game.protocol.codec.game

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.protocol.message.PacketLength
import com.veosps.game.protocol.message.ServerPacket
import com.veosps.game.protocol.message.ServerPacketStructureMap
import com.veosps.game.util.security.IsaacRandom
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

private val logger = InlineLogger()

class GameSessionEncoder(
    private val isaacRandom: IsaacRandom,
    private val structures: ServerPacketStructureMap
) : MessageToByteEncoder<ServerPacket>() {

    override fun encode(
        ctx: ChannelHandlerContext,
        msg: ServerPacket,
        out: ByteBuf
    ) {
        val structure = structures[msg]
        if (structure == null) {
            logger.error { "Structure for packet not defined (packet=$msg)" }
            return
        }
        val buf = ctx.alloc().buffer()
        structure.write(msg, buf)
        //logger.debug { "Sending packet to client (packet=$msg)" }

        val opcode = modifyOpcode(structure.opcode)
        out.writeByte(opcode)
        if (structure.length == PacketLength.Byte) {
            out.writeByte(buf.writerIndex())
        } else if (structure.length == PacketLength.Short) {
            out.writeShort(buf.writerIndex())
        }
        out.writeBytes(buf)
    }

    private fun modifyOpcode(opcode: Int): Int {
        return (opcode + isaacRandom.opcodeModifier()) and 0xFF
    }
}
