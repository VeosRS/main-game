package com.veosps.game.cache.types

interface TypeBuilder<T : DataType> {
    fun build(): T
}