package com.veosps.game.protocol.update.player.task

import com.veosps.game.event.impl.StatLevelUp
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.mob.PlayerList
import com.veosps.game.models.entities.player.stat.Stat
import com.veosps.game.models.entities.player.stat.StatMap
import com.veosps.game.models.entities.player.stat.baseLevel
import com.veosps.game.models.item.Item
import com.veosps.game.models.item.container.ItemContainerMap
import com.veosps.game.models.ui.sendVarp
import com.veosps.game.models.vars.VarpMap
import com.veosps.game.protocol.packet.server.UpdateInvFull
import com.veosps.game.protocol.packet.server.UpdateInvPartial
import com.veosps.game.protocol.packet.server.UpdateStat
import com.veosps.game.protocol.update.task.UpdateTask
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private const val INV_PARTIAL_BYTES_PER_ITEM = Short.SIZE_BYTES + Short.SIZE_BYTES + Byte.SIZE_BYTES
private const val INV_FULL_BYTES_PER_ITEM = Short.SIZE_BYTES + Byte.SIZE_BYTES

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class PlayerPostUpdateTask(
    private val playerList: PlayerList
) : UpdateTask {

    override suspend fun execute() {
        playerList.forEach { player ->
            if (player == null) {
                return@forEach
            }
            player.removeVarps()
            player.updateVarps()
            player.removeContainers()
            player.updateContainers()
            player.updateStats()
            player.entity.updates.clear()
            player.movement.nextSteps.clear()
            player.displace = false
            player.moveRequest = null
            player.snapshot = player.snapshot()
        }
    }
}

private fun Player.removeContainers(
    oldContainers: ItemContainerMap = snapshot.containers,
    curContainers: ItemContainerMap = containers
) {
    oldContainers.forEach { (key, container) ->
        /* skip containers that do not auto-update */
        if (!container.autoUpdate) {
            return@forEach
        }
        /* if container has been seen previously but is no longer mapped - send it as empty */
        val removed = !curContainers.containsKey(key)
        if (removed) {
            val packet = UpdateInvFull(
                key = key.clientId ?: -1,
                component = key.component?.packed ?: 0,
                items = emptyList()
            )
            write(packet)
        }
    }
}

private fun Player.updateContainers(
    oldContainers: ItemContainerMap = snapshot.containers,
    curContainers: ItemContainerMap = containers
) {
    curContainers.forEach { (key, cur) ->
        /* skip containers that do not auto-update */
        if (!cur.autoUpdate) {
            return@forEach
        }

        /* if container has not been previously sent - send a full update */
        val old = oldContainers[key]
        if (old == null) {
            val packet = UpdateInvFull(
                key = key.clientId ?: -1,
                component = key.component?.packed ?: -1,
                items = cur
            )
            write(packet)
            return@forEach
        }

        /* iterate and compare last-known container with current container */
        val maximumCapacity = old.size.coerceAtLeast(cur.size)
        var updatedItems: MutableMap<Int, Item?>? = null
        repeat(maximumCapacity) { slot ->
            val oldItem = if (slot in old.indices) old[slot] else null
            val curItem = if (slot in cur.indices) cur[slot] else null
            val matchId = oldItem?.id == curItem?.id
            val matchAmount = oldItem?.amount == curItem?.amount
            if (!matchId || !matchAmount) {
                val updated = updatedItems ?: mutableMapOf()
                updated[slot] = curItem
                updatedItems = updated
            }
        }

        /* if container has been updated since last seen - send appropriate update */
        updatedItems?.let { updated ->
            /* only send partial update when there's bandwidth to be saved */
            val partialBytes = updated.size * INV_PARTIAL_BYTES_PER_ITEM
            val fullBytes = cur.size * INV_FULL_BYTES_PER_ITEM
            val packet = if (partialBytes < fullBytes) {
                UpdateInvPartial(
                    key = key.clientId ?: -1,
                    component = key.component?.packed ?: -1,
                    updated = updated
                )
            } else {
                UpdateInvFull(
                    key = key.clientId ?: -1,
                    component = key.component?.packed ?: -1,
                    items = cur
                )
            }
            write(packet)
        }
    }
}

private fun Player.updateStats(
    oldStats: StatMap = snapshot.stats,
    curStats: StatMap = stats
) {
    curStats.forEach { (key, cur) ->
        val old = oldStats[key] ?: Stat.ZERO
        val updateXp = cur.experience != old.experience
        val updateLvl = cur.currLevel != old.currLevel
        if (updateLvl || updateXp) {
            val packet = UpdateStat(
                skill = key.id,
                currLevel = cur.currLevel,
                xp = cur.experience.toInt()
            )
            write(packet)
        }
        if (updateXp) {
            val oldBase = old.baseLevel()
            val currBase = cur.baseLevel()
            if (currBase > oldBase) {
                val event = StatLevelUp(this, key, oldBase, currBase, cur.experience)
                submitEvent(event)
            }
        }
    }
}

private fun Player.removeVarps(
    oldVarps: VarpMap = snapshot.varps,
    curVarps: VarpMap = varpMap
) {
    oldVarps.forEach { (varp, _) ->
        val removed = !curVarps.containsKey(varp)
        if (removed) {
            sendVarp(varp, 0)
        }
    }
}

private fun Player.updateVarps(
    oldVarps: VarpMap = snapshot.varps,
    curVarps: VarpMap = varpMap
) {
    curVarps.forEach { (varp, cur) ->
        val old = oldVarps[varp] ?: 0
        val update = old != cur
        if (update) {
            sendVarp(varp, cur)
        }
    }
}
