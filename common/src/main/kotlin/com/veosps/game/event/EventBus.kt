package com.veosps.game.event

import com.veosps.game.event.action.EventAction
import com.veosps.game.event.action.EventActionBuilder
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class EventBus(
    val events: MutableMap<KClass<out Event>, MutableList<EventAction<*>>> = mutableMapOf()
) : Map<KClass<out Event>, List<EventAction<*>>> by events {

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Event> publish(event: T): Boolean {
        val events = (events[event::class] as? List<EventAction<T>>) ?: return false
        val filtered = events.filter { it.where(event) }
        filtered.forEach {
            it.then(event)
        }
        return filtered.isNotEmpty()
    }

    inline fun <reified T : Event> subscribe(): EventActionBuilder<T> =
        EventActionBuilder(events.computeIfAbsent(T::class) { mutableListOf() })
}
