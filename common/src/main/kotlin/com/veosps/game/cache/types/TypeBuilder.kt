package com.veosps.game.cache.types

interface TypeBuilder<T : CacheType> {
    fun build(): T
}