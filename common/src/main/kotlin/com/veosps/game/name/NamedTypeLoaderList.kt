package com.veosps.game.name

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.cache.name.ui.UserInterfaceNameLoader
import com.veosps.game.cache.name.vars.VarbitNameLoader
import com.veosps.game.cache.name.vars.VarpNameLoader
import com.veosps.game.util.BeanScope
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private val logger = InlineLogger()

class NamedTypeLoaderList(
    private val loaders: MutableList<NamedTypeLoader> = mutableListOf()
) : List<NamedTypeLoader> by loaders {

    fun register(loader: NamedTypeLoader) {
        loaders.add(loader)
        logger.debug { "Register named type loader (loader=$loader)" }
    }

    operator fun NamedTypeLoader.unaryMinus() {
        register(this)
    }
}

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class NamedTypeLoaderListProvider(
    private val interfaceList: UserInterfaceNameLoader,
    private val varpList: VarpNameLoader,
    private val varbitList: VarbitNameLoader
) : FactoryBean<NamedTypeLoaderList> {

    override fun getObjectType() = NamedTypeLoaderList::class.java

    override fun getObject() = NamedTypeLoaderList().apply {
        register(interfaceList)
        register(varpList)
        register(varbitList)
    }
}