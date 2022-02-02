package com.veosps.game.plugins.fundamentals.protocol.structure

import com.veosps.game.cache.GameCache
import com.veosps.game.cache.buffer.readIntIME
import com.veosps.game.cache.buffer.readIntME
import com.veosps.game.protocol.packet.login.AuthCode
import com.veosps.game.protocol.packet.login.CacheChecksum
import com.veosps.game.protocol.packet.login.LoginPacketMap

val packets: LoginPacketMap by inject()
val cache: GameCache by inject()

packets.register {
    val code = when (readByte().toInt()) {
        2 -> {
            skipBytes(Int.SIZE_BYTES)
            -1
        }
        1, 3 -> {
            val auth = readUnsignedMedium()
            skipBytes(Byte.SIZE_BYTES)
            auth
        }
        else -> readInt()
    }
    AuthCode(code)
}

packets.register {
    val crcs = IntArray(cache.archiveCount)
    crcs[6] = readIntLE()
    crcs[15] = readInt()
    crcs[14] = readIntIME()
    crcs[7] = readIntME()
    crcs[20] = readIntLE()
    crcs[17] = readIntIME()
    crcs[8] = readIntIME()
    crcs[18] = readIntLE()
    crcs[2] = readIntME()
    crcs[16] = readIntLE()
    crcs[12] = readIntLE()
    crcs[9] = readIntLE()
    crcs[1] = readInt()
    crcs[0] = readInt()
    crcs[10] = readIntLE()
    crcs[5] = readInt()
    crcs[19] = readIntIME()
    crcs[4] = readIntIME()
    crcs[3] = readIntIME()
    crcs[13] = readInt()
    crcs[11] = readInt()
    CacheChecksum(crcs)
}
