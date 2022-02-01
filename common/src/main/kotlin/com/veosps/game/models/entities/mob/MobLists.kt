package com.veosps.game.models.entities.mob

import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private const val MAX_PLAYER_COUNT = 2047
private const val MAX_NPC_COUNT = 32767

private const val INVALID_INDEX = -1
private const val INDEX_PADDING = 1

private inline fun <reified T> createList(count: Int): MutableList<T?> = arrayOfNulls<T>(count).toMutableList()

private inline fun <reified T> List<T>.freeIndex(): Int {
    for (i in INDEX_PADDING until indices.last) {
        if (this[i] == null) {
            return i
        }
    }
    return INVALID_INDEX
}

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class PlayerList(
    private val players: MutableList<Player?> = createList(MAX_PLAYER_COUNT)
) : List<Player?> by players {

    override val size: Int
        get() = players.count { it != null }

    val indices: IntRange
        get() = players.indices

    val capacity: Int
        get() = players.size

    fun register(player: Player): Boolean {
        val index = players.freeIndex()
        if (index == INVALID_INDEX) {
            return false
        }
        players[index] = player
        player.index = index
        return true
    }

    fun remove(player: Player): Boolean = when {
        player.index == INVALID_INDEX -> false
        players[player.index] != player -> false
        else -> {
            players[player.index] = null
            true
        }
    }

    override fun isEmpty(): Boolean = size == 0
}