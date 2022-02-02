package com.veosps.game.plugin.kotlin

import com.veosps.game.action.ActionBus
import com.veosps.game.event.EventBus
import io.github.classgraph.ClassGraph
import org.springframework.beans.factory.BeanFactory
import org.springframework.stereotype.Component

@Component
class KotlinPluginLoader(
    private val beanFactory: BeanFactory,
    private val eventBus: EventBus,
    private val actionBus: ActionBus
) {

    fun load(): List<KotlinPlugin> {
        val plugins = mutableListOf<KotlinPlugin>()
        ClassGraph().enableAllInfo().scan().use { result ->
            val subclasses = result.getSubclasses(KotlinPlugin::class.java.name).directOnly()
            subclasses.forEach { subclass ->
                val loadedClass = subclass.loadClass(KotlinPlugin::class.java)
                val constructor = loadedClass.getConstructor(
                    BeanFactory::class.java,
                    EventBus::class.java,
                    ActionBus::class.java
                )
                try {
                    val instance = constructor.newInstance(beanFactory, eventBus, actionBus)
                    plugins.add(instance)
                } catch (t: Throwable) {
                    throw t.cause ?: t
                }
            }
        }
        return plugins
    }
}