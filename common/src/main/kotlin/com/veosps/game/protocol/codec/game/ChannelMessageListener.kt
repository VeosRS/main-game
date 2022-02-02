package com.veosps.game.protocol.codec.game

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.protocol.message.ServerPacket
import com.veosps.game.protocol.message.ServerPacketListener
import com.veosps.game.protocol.packet.server.*
import io.netty.channel.Channel

private val logger = InlineLogger()

class ChannelMessageListener(
    private val channel: Channel
) : ServerPacketListener {

    private val validPackets = setOf(
        RebuildNormal::class,
        //PlayerInfo::class,
        IfOpenTop::class,
        //IfOpenSub::class,
        //ResetClientVarCache::class,
        //ResetAnims::class,
        //IfSetText::class,
        //NpcInfoSmallViewport::class,
        //UpdateStat::class,
        //MessageGame::class,
        //UpdateRunEnergy::class,
        //VarpSmall::class,
        //VarpLarge::class,
        //RunClientScript::class
    )

    override fun write(packet: ServerPacket) {
        if (packet::class !in validPackets) return

        logger.debug { "Writing Packet: $packet" }

        channel.write(packet)
    }

    override fun flush() {
        channel.flush()
    }

    override fun close() {
        channel.close()
    }
}
