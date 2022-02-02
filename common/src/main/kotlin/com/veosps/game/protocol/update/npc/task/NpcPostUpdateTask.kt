package com.veosps.game.protocol.update.npc.task

import com.veosps.game.models.entities.mob.NpcList
import com.veosps.game.protocol.update.task.UpdateTask
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class NpcPostUpdateTask(
    private val npcList: NpcList
) : UpdateTask {

    override suspend fun execute() {
        npcList.forEach { npc ->
            if (npc == null) {
                return@forEach
            }
            npc.entity.updates.clear()
            npc.movement.nextSteps.clear()
            npc.displace = false
        }
    }
}
