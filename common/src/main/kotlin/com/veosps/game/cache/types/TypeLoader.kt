package com.veosps.game.cache.types

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.cache.GameCache
import com.veosps.game.util.BeanScope
import io.netty.buffer.ByteBuf
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private val logger = InlineLogger()

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
interface TypeLoader {
    val cache: GameCache
    fun load()
}

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class CacheTypeLoaderList(
    private val loaders: MutableList<TypeLoader> = mutableListOf()
) : List<TypeLoader> by loaders {

    fun register(loader: TypeLoader) {
        logger.debug { "Register cache type loader (type=${loader.javaClass.simpleName})" }
        loaders.add(loader)
    }
}
