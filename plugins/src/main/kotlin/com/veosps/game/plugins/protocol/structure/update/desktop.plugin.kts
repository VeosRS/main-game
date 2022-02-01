package com.veosps.game.plugins.protocol.structure.update

import com.veosps.game.cache.buffer.writeByteSub
import com.veosps.game.cache.buffer.writeBytesReversed
import com.veosps.game.cache.buffer.writeShortAdd
import com.veosps.game.cache.buffer.writeString
import com.veosps.game.protocol.Device
import com.veosps.game.protocol.packet.update.*
import com.veosps.game.protocol.structure.DevicePacketStructureMap

val structures: DevicePacketStructureMap by inject()
val masks = structures.playerUpdate(Device.Desktop)

masks.order {
    -DirectionMask::class
    -AppearanceMask::class
    -MovementTempMask::class
    -MovementPermMask::class
}

masks.register<BitMask> {
    mask = 128
    write {
        if (packed >= 255) {
            val bitmask = packed or mask
            it.writeByte(bitmask and 255)
            it.writeByte(bitmask shr 8)
        } else {
            it.writeByte(packed and 255)
        }
    }
}

masks.register<DirectionMask> {
    mask = 32
    write {
        it.writeShortAdd(angle)
    }
}

masks.register<MovementPermMask> {
    mask = 2048
    write {
        it.writeByte(type)
    }
}

masks.register<MovementTempMask> {
    mask = 4096
    write {
        it.writeByteSub(type)
    }
}

masks.register<AppearanceMask> {
    mask = 1
    write {
        val appBuf = it.alloc().buffer()
        appBuf.writeByte(gender)
        appBuf.writeByte(skull)
        appBuf.writeByte(overheadPrayer)

        if (npc > 0) {
            appBuf.writeShort(-1)
            appBuf.writeShort(npc)
        } else {
            appBuf.writeBytes(looks)
        }

        colors.forEach { color ->
            appBuf.writeByte(color)
        }

        bas.forEach { animation ->
            appBuf.writeShort(animation)
        }

        appBuf.writeString(username)
        appBuf.writeByte(combatLevel)
        appBuf.writeShort(0) /* unknown */
        appBuf.writeBoolean(invisible)

        it.writeByte(appBuf.writerIndex())
        it.writeBytesReversed(appBuf)
    }
}
