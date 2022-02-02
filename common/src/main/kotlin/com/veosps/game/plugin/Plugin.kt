package com.veosps.game.plugin

import com.veosps.game.action.Action
import com.veosps.game.action.ActionBus
import com.veosps.game.action.ActionExecutor
import com.veosps.game.cache.name.vars.VarpNameMap
import com.veosps.game.event.Event
import com.veosps.game.event.EventBus
import com.veosps.game.models.vars.type.VarpType
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import kotlin.properties.ObservableProperty

open class Plugin(
    val injector: BeanFactory,
    val eventBus: EventBus,
    val actionBus: ActionBus,
) {
    private val varpNames: VarpNameMap by inject()

    inline fun <reified T : Action> onAction(id: Int, noinline executor: ActionExecutor<T>) =
        onAction(id.toLong(), executor)

    inline fun <reified T : Action> onAction(id: Long, noinline executor: ActionExecutor<T>) {
        val registered = actionBus.register(id, executor)
        if (!registered) {
            error("Action with id has already been set (id=$id, type=${T::class.simpleName})")
        }
    }

    inline fun <reified T : Event> onEvent() = eventBus.subscribe<T>()

    fun varp(name: String): VarpType {
        return varpNames[name] ?: error("Varp with name \"$name\" not found.")
    }

    inline fun <reified T : Any> inject(): ObservableProperty<T> = InjectedProperty(injector.getBean<T>())

    class InjectedProperty<T>(value: T) : ObservableProperty<T>(value)
}