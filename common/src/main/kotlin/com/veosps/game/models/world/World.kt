package com.veosps.game.models.world

import com.veosps.game.collision.CollisionMap
import com.veosps.game.models.obj.GameObjectMap
import com.veosps.game.queue.GameQueueList
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class World(
    val collisionMap: CollisionMap,
    val objectMap: GameObjectMap
) {

    internal val queueList = GameQueueList()

    fun queue(block: suspend () -> Unit) = queueList.queue(block)
}
