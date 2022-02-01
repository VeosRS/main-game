package com.veosps.game.models.obj

import com.veosps.game.models.map.Coordinates
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private class GameObjectList(
    private val objects: MutableMap<Int, GameObject> = mutableMapOf()
) : MutableMap<Int, GameObject> by objects

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class GameObjectMap private constructor(
    private val objects: MutableMap<Coordinates, GameObjectList>,
    private val dynamicSpawned: MutableMap<Coordinates, GameObjectList>,
    private val staticRemoved: MutableMap<Coordinates, GameObjectList>
) {

    /* used for dependency injection */
    @Suppress("UNUSED")
    constructor() : this(mutableMapOf(), mutableMapOf(), mutableMapOf())

    fun addStatic(obj: GameObject) {
        objects.put(obj)
    }

    fun addDynamic(obj: GameObject) {
        objects.put(obj)
        dynamicSpawned.put(obj)
    }

    fun remove(obj: GameObject) {
        val dynamicRemoved = dynamicSpawned.remove(obj.coords, obj.shape)
        val objectRemoved = objects.remove(obj.coords, obj.shape)

        /* if object is removed and it's not a dynamic object, assume it's a static object */
        if (objectRemoved != null && dynamicRemoved == null) {
            staticRemoved.put(obj)
        }
    }

    fun get(coords: Coordinates, shape: Int): GameObject? {
        return objects[coords]?.values?.firstOrNull { it.shape == shape }
    }

    operator fun get(coords: Coordinates): Collection<GameObject> {
        return objects[coords]?.values ?: emptyList()
    }

    private fun MutableMap<Coordinates, GameObjectList>.put(obj: GameObject) {
        val objects = getOrPut(obj.coords) { GameObjectList() }
        objects[obj.shape] = obj
    }

    private fun MutableMap<Coordinates, GameObjectList>.remove(coords: Coordinates, shape: Int): GameObject? {
        val objects = this[coords] ?: return null
        return objects.remove(shape)
    }
}
