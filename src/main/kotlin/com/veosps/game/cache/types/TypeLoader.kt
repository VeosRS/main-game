package com.veosps.game.cache.types

import com.veosps.game.cache.GameCache
import io.netty.buffer.ByteBuf

interface TypeLoader<T : DataType> {
    val cache: GameCache
    val types: MutableList<T>
    fun load()
    fun ByteBuf.readType(id: Int): T
}