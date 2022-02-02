package com.veosps.game.protocol.packet.server

import com.veosps.game.protocol.message.ServerPacket

@Suppress("INLINE_CLASS_DEPRECATED")
inline class MinimapFlagSet(private val packed: Int) : ServerPacket {

    val x: Int
        get() = packed and 0xFFFF

    val y: Int
        get() = (packed shr 16) and 0xFFFF

    constructor(x: Int, y: Int) : this(
        (x and 0xFFFF) or ((y and 0xFFFF) shl 16)
    )
}
