package com.veosps.game.cache.types

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.cache.GameCache
import com.veosps.game.cache.types.item.ItemTypeLoader
import com.veosps.game.cache.types.ui.ComponentTypeLoader
import com.veosps.game.cache.types.vars.VarbitTypeLoader
import com.veosps.game.cache.types.vars.VarpTypeLoader
import com.veosps.game.util.BeanScope
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private val logger = InlineLogger()

interface TypeLoader {
    val cache: GameCache
    fun load()
}

class CacheTypeLoaderList(
    private val loaders: MutableList<TypeLoader> = mutableListOf()
) : List<TypeLoader> by loaders {

    fun register(loader: TypeLoader) {
        logger.debug { "Register cache type loader (type=${loader.javaClass.simpleName})" }
        loaders.add(loader)
    }
}

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class CacheTypeLoaderListProvider(
    private val itemLoader: ItemTypeLoader,
    private val varpLoader: VarpTypeLoader,
    private val varbitLoader: VarbitTypeLoader,
    private val componentLoader: ComponentTypeLoader
) : FactoryBean<CacheTypeLoaderList> {

    override fun getObjectType() = CacheTypeLoaderList::class.java

    override fun getObject() = CacheTypeLoaderList().apply {
        register(itemLoader)
        register(varpLoader)
        register(varbitLoader)
        register(componentLoader)
    }
}