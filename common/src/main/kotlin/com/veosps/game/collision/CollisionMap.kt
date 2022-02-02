package com.veosps.game.collision

import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.world.move.Direction
import com.veosps.game.models.world.move.translate
import com.veosps.game.util.BeanScope
import org.rsmod.pathfinder.collision.CollisionStrategies
import org.rsmod.pathfinder.collision.CollisionStrategy
import org.rsmod.pathfinder.flag.CollisionFlag
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class CollisionMap(
    private val tiles: MutableMap<Coordinates, CollisionTile> = mutableMapOf()
) {

    fun add(coordinates: Coordinates, mask: Int) {
        val old = tiles[coordinates]?.flags ?: 0
        this[coordinates] = old or mask
    }

    fun remove(coordinates: Coordinates, mask: Int) {
        val old = tiles[coordinates]?.flags ?: 0
        this[coordinates] = old and mask.inv()
    }

    operator fun get(coordinates: Coordinates): Int? {
        return tiles[coordinates]?.flags
    }

    operator fun set(coordinates: Coordinates, flags: Int) {
        tiles[coordinates] = CollisionTile(flags)
    }
}

fun CollisionMap.buildFlags(center: Coordinates, size: Int): IntArray {
    val halfSize = size / 2
    val flags = IntArray(size * size)
    val rangeX = center.x - halfSize until center.x + halfSize
    val rangeY = center.y - halfSize until center.y + halfSize
    for (y in rangeY) {
        for (x in rangeX) {
            val coords = Coordinates(x, y, center.level)
            val flag = this[coords] ?: 0
            val localX = x - (center.x - halfSize)
            val localY = y - (center.y - halfSize)
            val index = (localY * size) + localX
            flags[index] = flag
        }
    }
    return flags
}

fun CollisionMap.canTraverse(
    src: Coordinates,
    dir: Direction,
    strategy: CollisionStrategy = CollisionStrategies.Normal
): Boolean {
    // TODO: support for size2/n
    val dest = src.translate(dir)
    val flag = this[dest] ?: 0
    return when (dir) {
        Direction.West -> strategy.canMove(flag, CollisionFlag.BLOCK_WEST)
        Direction.East -> strategy.canMove(flag, CollisionFlag.BLOCK_EAST)
        Direction.North -> strategy.canMove(flag, CollisionFlag.BLOCK_NORTH)
        Direction.South -> strategy.canMove(flag, CollisionFlag.BLOCK_SOUTH)
        Direction.SouthWest ->
            strategy.canMove(flag, CollisionFlag.BLOCK_SOUTH_WEST) &&
                    strategy.canMove(flag, CollisionFlag.BLOCK_WEST) &&
                    strategy.canMove(flag, CollisionFlag.BLOCK_SOUTH)
        Direction.SouthEast ->
            strategy.canMove(flag, CollisionFlag.BLOCK_SOUTH_EAST) &&
                    strategy.canMove(flag, CollisionFlag.BLOCK_EAST) &&
                    strategy.canMove(flag, CollisionFlag.BLOCK_SOUTH)
        Direction.NorthWest ->
            strategy.canMove(flag, CollisionFlag.BLOCK_NORTH_WEST) &&
                    strategy.canMove(flag, CollisionFlag.BLOCK_WEST) &&
                    strategy.canMove(flag, CollisionFlag.BLOCK_NORTH)
        Direction.NorthEast ->
            strategy.canMove(flag, CollisionFlag.BLOCK_NORTH_EAST) &&
                    strategy.canMove(flag, CollisionFlag.BLOCK_EAST) &&
                    strategy.canMove(flag, CollisionFlag.BLOCK_NORTH)
    }
}