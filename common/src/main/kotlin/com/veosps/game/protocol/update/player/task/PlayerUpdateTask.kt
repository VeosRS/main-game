package com.veosps.game.protocol.update.player.task

import com.github.michaelbull.logging.InlineLogger
import com.google.common.primitives.Ints.min
import com.veosps.game.cache.buffer.BitBuf
import com.veosps.game.cache.buffer.toBitBuf
import com.veosps.game.coroutines.IoCoroutineScope
import com.veosps.game.models.Client
import com.veosps.game.models.ClientDevice
import com.veosps.game.models.ClientList
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.mob.PlayerList
import com.veosps.game.models.map.BuildArea
import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.map.isWithinDistance
import com.veosps.game.models.world.move.Direction
import com.veosps.game.protocol.Device
import com.veosps.game.protocol.packet.server.PlayerInfo
import com.veosps.game.protocol.packet.update.AppearanceMask
import com.veosps.game.protocol.packet.update.BitMask
import com.veosps.game.protocol.packet.update.DirectionMask
import com.veosps.game.protocol.structure.DevicePacketStructureMap
import com.veosps.game.protocol.update.mask.UpdateMask
import com.veosps.game.protocol.update.mask.UpdateMaskPacketMap
import com.veosps.game.protocol.update.of
import com.veosps.game.protocol.update.player.mask.of
import com.veosps.game.protocol.update.record.UpdateRecord
import com.veosps.game.protocol.update.task.UpdateTask
import com.veosps.game.util.BeanScope
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import kotlin.math.abs

private val logger = InlineLogger()

private const val MAX_PLAYER_ADDITIONS_PER_CYCLE = 40
private const val MAX_LOCAL_PLAYERS = 255

private val DIRECTION_ROT = mapOf(
    Direction.SouthWest to 0,
    Direction.South to 1,
    Direction.SouthEast to 2,
    Direction.West to 3,
    Direction.East to 4,
    Direction.NorthWest to 5,
    Direction.North to 6,
    Direction.NorthEast to 7
)

private val DIRECTION_DIFF_X = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)
private val DIRECTION_DIFF_Y = intArrayOf(-1, -1, -1, 0, 0, 1, 1, 1)

private sealed class UpdateGroup {
    object Active : UpdateGroup()
    object Inactive : UpdateGroup()
}

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class PlayerUpdateTask(
    private val clientList: ClientList,
    private val playerList: PlayerList,
    private val ioCoroutine: IoCoroutineScope,
    private val devicePackets: DevicePacketStructureMap
) : UpdateTask {

    fun initClient(client: Client) {
        client.addPublicRecords()
    }

    override suspend fun execute() = ioCoroutine.launch { launchGpi() }.join()

    private fun CoroutineScope.launchGpi() = launch {
        clientList.forEach { client ->
            launch {
                val buf = client.gpiBuffer()
                val info = PlayerInfo(buf)
                client.player.write(info)
                client.playerRecords.forEach(::group)
            }
        }
    }

    private fun Client.gpiBuffer(): ByteBuf {
        val mainBuf = bufAllocator.buffer()
        val maskBuf = bufAllocator.buffer()
        val masks = device.maskPackets
        mainBuf.getPlayerInfo(player, playerRecords, maskBuf, masks)
        mainBuf.writeBytes(maskBuf)
        return mainBuf
    }

    private fun ByteBuf.getPlayerInfo(
        player: Player,
        records: MutableList<UpdateRecord>,
        maskBuf: ByteBuf,
        maskPackets: UpdateMaskPacketMap
    ) {
        var local = 0
        var added = 0
        local += records.localPlayerInfo(toBitBuf(), player, maskBuf, maskPackets, UpdateGroup.Active)
        local += records.localPlayerInfo(toBitBuf(), player, maskBuf, maskPackets, UpdateGroup.Inactive)
        added += records.worldPlayerInfo(toBitBuf(), player, maskBuf, maskPackets, UpdateGroup.Inactive, local, added)
        added += records.worldPlayerInfo(toBitBuf(), player, maskBuf, maskPackets, UpdateGroup.Active, local, added)
    }

    private fun List<UpdateRecord>.localPlayerInfo(
        bitBuf: BitBuf,
        player: Player,
        maskBuf: ByteBuf,
        maskPackets: UpdateMaskPacketMap,
        group: UpdateGroup
    ): Int {
        var skipCount = 0
        var localPlayers = 0
        forEach { record ->
            val (index, flag, local) = record
            val inGroup = local && (group.bit and 0x1) == flag
            if (!inGroup) {
                return@forEach
            }
            if (skipCount > 0) {
                skipCount--
                record.flag = (record.flag or 0x2)
                return@forEach
            }
            localPlayers++
            val localPlayer = playerList[index]
            if (localPlayer == null || localPlayer != player && !player.canView(localPlayer)) {
                record.reset = true
                bitBuf.removeLocalPlayer(localPlayer, record)
                return@forEach
            }
            val maskUpdate = localPlayer.isMaskUpdateRequired()
            val moveUpdate = localPlayer.isMoving()
            if (maskUpdate) {
                maskBuf.writeMaskUpdate(localPlayer.entity.updates, maskPackets)
            }
            if (maskUpdate || moveUpdate) {
                bitBuf.writeBoolean(true)
            }
            when {
                moveUpdate -> bitBuf.writeLocalMovement(localPlayer, maskUpdate)
                maskUpdate -> bitBuf.writeMaskUpdateSignal()
                else -> {
                    record.flag = (record.flag or 0x2)
                    skipCount = localSkipCount(group, index + 1)
                    bitBuf.writeSkipCount(skipCount)
                }
            }
        }
        return localPlayers
    }

    private fun List<UpdateRecord>.worldPlayerInfo(
        bitBuf: BitBuf,
        player: Player,
        maskBuf: ByteBuf,
        maskPackets: UpdateMaskPacketMap,
        group: UpdateGroup,
        localCount: Int,
        previouslyAdded: Int
    ): Int {
        var added = 0
        var skipCount = 0
        forEach { record ->
            val (index, flag, local) = record
            val inGroup = !local && (group.bit and 0x1) == flag
            if (!inGroup) {
                return@forEach
            }
            if (skipCount > 0) {
                skipCount--
                record.flag = (record.flag or 0x2)
                return@forEach
            }
            val globalPlayer = if (index in playerList.indices) playerList[index] else null
            if (globalPlayer != null) {
                val capacityReached = added + previouslyAdded >= MAX_PLAYER_ADDITIONS_PER_CYCLE ||
                        localCount >= MAX_LOCAL_PLAYERS
                if (player.canView(globalPlayer) && !capacityReached) {
                    bitBuf.writePlayerAddition(globalPlayer, record)
                    maskBuf.writeNewPlayerMasks(globalPlayer, maskPackets)
                    record.flag = (record.flag or 0x2)
                    record.local = true
                    record.coordinates = globalPlayer.coordinates.packed18Bits
                    added++
                }
                return@forEach
            }
            record.flag = (record.flag or 0x2)
            skipCount = globalSkipCount(player, group, index + 1)
            bitBuf.writeSkipCount(skipCount)
        }
        return added
    }

    private fun ByteBuf.writeNewPlayerMasks(
        other: Player,
        handlers: UpdateMaskPacketMap
    ) {
        val updates = other.entity.updates
        if (!updates.contains(AppearanceMask::class)) {
            updates.add(AppearanceMask.of(other))
        }
        if (!updates.contains(DirectionMask::class)) {
            updates.add(DirectionMask.of(other))
        }
        writeMaskUpdate(updates, handlers)
    }

    private fun ByteBuf.writeMaskUpdate(
        masks: Set<UpdateMask>,
        handlers: UpdateMaskPacketMap
    ) {
        var bitmask = 0
        masks.forEach { mask ->
            val handler = handlers.getValue(mask)
            bitmask = bitmask or handler.mask
        }
        writeMaskBit(bitmask, handlers)
        handlers.order.forEach { ordered ->
            val mask = masks.firstOrNull { it::class == ordered } ?: return@forEach
            val handler = handlers.getValue(mask)
            handler.write(mask, this)
        }
    }

    private fun BitBuf.writePlayerAddition(other: Player, record: UpdateRecord) {
        val currMultiplier = other.coordinates.packed18Bits
        val lastMultiplier = record.coordinates
        val multiplierChange = currMultiplier != lastMultiplier
        writeBoolean(true)
        writeBits(value = 0, amount = 2)
        writeBoolean(multiplierChange)
        if (multiplierChange) {
            writeCoordinateMultiplier(lastMultiplier, currMultiplier)
        }
        writeBits(value = other.coordinates.x, amount = 13)
        writeBits(value = other.coordinates.y, amount = 13)
        writeBoolean(true)
    }

    private fun BitBuf.writeLocalMovement(local: Player, maskUpdate: Boolean) {
        val currCoords = local.coordinates
        val lastCoords = local.snapshot.coords
        val diffX = currCoords.x - lastCoords.x
        val diffY = currCoords.y - lastCoords.y
        val diffLevel = currCoords.level - lastCoords.level
        val largeChange = abs(diffX) >= BuildArea.REBUILD_BOUNDARY || abs(diffY) >= BuildArea.REBUILD_BOUNDARY
        val teleport = largeChange || local.displace

        writeBoolean(maskUpdate)
        if (teleport) {
            writeBits(value = 3, amount = 2)
            writeBoolean(largeChange)
            writeBits(value = diffLevel and 0x3, amount = 2)
            if (largeChange) {
                writeBits(value = diffX and 0x3FFF, amount = 14)
                writeBits(value = diffY and 0x3FFF, amount = 14)
            } else {
                writeBits(value = diffX and 0x1F, amount = 5)
                writeBits(value = diffY and 0x1F, amount = 5)
            }
        } else {
            val steps = local.movement.nextSteps
            val walkStep = steps.firstOrNull()
            val runStep = if (steps.size > 1) steps[1] else null
            val walkRot = DIRECTION_ROT[walkStep?.dir] ?: 0
            val runRot = DIRECTION_ROT[runStep?.dir]
            var dx = DIRECTION_DIFF_X[walkRot]
            var dy = DIRECTION_DIFF_Y[walkRot]

            var running = false
            var direction = 0
            if (runRot != null) {
                dx += DIRECTION_DIFF_X[runRot]
                dy += DIRECTION_DIFF_Y[runRot]
                val runDir = runDir(dx, dy)
                if (runDir != null) {
                    direction = runDir
                    running = true
                }
            }
            if (!running) {
                val walkDir = walkDir(dx, dy)
                if (walkDir != null) {
                    direction = walkDir
                }
            }
            writeBits(value = if (running) 2 else 1, amount = 2)
            writeBits(value = direction, amount = if (running) 4 else 3)
        }
    }

    private fun BitBuf.writeMaskUpdateSignal() {
        writeBits(value = 1, amount = 1)
        writeBits(value = 0, amount = 2)
    }

    private fun BitBuf.removeLocalPlayer(local: Player?, record: UpdateRecord) {
        val newCoordinates = local?.coordinates?.packed18Bits ?: 0
        val coordinateChange = newCoordinates != record.coordinates
        writeBoolean(true)
        writeBoolean(false)
        writeBits(value = 0, amount = 2)
        writeBoolean(coordinateChange)
        if (coordinateChange) {
            writeCoordinateMultiplier(record.coordinates, newCoordinates)
        }
    }

    private fun BitBuf.writeSkipCount(count: Int) {
        writeBits(value = 0, amount = 1)
        when {
            count == 0 -> writeBits(value = count, amount = 2)
            count < 32 -> {
                writeBits(value = 1, amount = 2)
                writeBits(value = count, amount = 5)
            }
            count < 256 -> {
                writeBits(value = 2, amount = 2)
                writeBits(value = count, amount = 8)
            }
            else -> {
                val cap = playerList.capacity
                if (count > cap) {
                    logger.error { "Skip count out-of-range (count=$count, cap=$cap)" }
                }
                writeBits(value = 3, amount = 2)
                writeBits(value = min(cap, count), amount = 11)
            }
        }
    }

    private fun BitBuf.writeCoordinateMultiplier(oldMultiplier: Int, newMultiplier: Int) {
        val currMultiplierY = newMultiplier and 0xFF
        val currMultiplierX = (newMultiplier shr 8) and 0xFF
        val currLevel = (newMultiplier shr 16) and 0x3

        val lastMultiplierY = oldMultiplier and 0xFF
        val lastMultiplierX = (oldMultiplier shr 8) and 0xFF
        val lastLevel = (oldMultiplier shr 16) and 0x3

        val diffX = currMultiplierX - lastMultiplierX
        val diffY = currMultiplierY - lastMultiplierY
        val diffLevel = currLevel - lastLevel

        val levelChange = diffLevel != 0
        val smallChange = abs(diffX) <= 1 && abs(diffY) <= 1
        when {
            levelChange -> {
                writeBits(value = 1, amount = 2)
                writeBits(value = diffLevel, amount = 2)
            }
            smallChange -> {
                val direction = when {
                    diffX == -1 && diffY == -1 -> 0
                    diffX == 1 && diffY == -1 -> 2
                    diffX == -1 && diffY == 1 -> 5
                    diffX == 1 && diffY == 1 -> 7
                    diffY == -1 -> 1
                    diffX == -1 -> 3
                    diffX == 1 -> 4
                    else -> 6
                }
                writeBits(value = 2, amount = 2)
                writeBits(value = diffLevel, amount = 2)
                writeBits(value = direction, amount = 3)
            }
            else -> {
                writeBits(value = 3, amount = 2)
                writeBits(value = diffLevel, amount = 2)
                writeBits(value = diffX and 0xFF, amount = 8)
                writeBits(value = diffY and 0xFF, amount = 8)
            }
        }
    }

    private fun List<UpdateRecord>.localSkipCount(
        group: UpdateGroup,
        offset: Int
    ): Int {
        var count = 0
        for (i in offset until indices.last) {
            val record = this[i]
            val (index, flag, local) = record
            val inGroup = local && (group.bit and 0x1) == flag
            if (!inGroup) {
                continue
            }
            val next = playerList[index] ?: break
            if (next.isUpdateRequired()) {
                break
            }
            count++
        }
        return count
    }

    private fun List<UpdateRecord>.globalSkipCount(
        player: Player,
        group: UpdateGroup,
        offset: Int
    ): Int {
        var count = 0
        for (i in offset until indices.last) {
            val record = this[i]
            val (index, flag, local, coordsMultiplier) = record
            val inGroup = !local && (group.bit and 0x1) == flag
            if (!inGroup) {
                continue
            }
            val next = playerList[index]
            if (next != null) {
                val withinViewDistance = player.canView(next)
                val multiplierChange = next.coordinates.packed18Bits != coordsMultiplier
                if (withinViewDistance || multiplierChange) {
                    break
                }
            }
            count++
        }
        return count
    }

    private fun ByteBuf.writeMaskBit(
        bitmask: Int,
        handlers: UpdateMaskPacketMap
    ) {
        val mask = BitMask(bitmask)
        val handler = handlers.getValue(mask)
        handler.write(mask, this)
    }

    private fun Player.canView(other: Player): Boolean {
        return !other.entity.appearance.invisible && other.coordinates.isWithinView(coordinates)
    }

    private fun Player.isUpdateRequired(): Boolean {
        return isMoving() || isMaskUpdateRequired()
    }

    private fun Player.isMoving(): Boolean {
        return movement.nextSteps.isNotEmpty() || displace
    }

    private fun Player.isMaskUpdateRequired(): Boolean {
        return entity.updates.isNotEmpty()
    }

    private fun Coordinates.isWithinView(coords: Coordinates): Boolean {
        return isWithinDistance(coords, BuildArea.REBUILD_BOUNDARY - 1)
    }

    private fun Client.addPublicRecords() {
        for (i in 1..playerList.capacity) {
            if (i == player.index) {
                addUpdateRecord(i, true, player.coordinates.packed18Bits)
                continue
            }
            addUpdateRecord(i)
        }
    }

    private fun Client.addUpdateRecord(
        index: Int,
        local: Boolean = false,
        coordinates: Int = 0
    ) {
        val record = UpdateRecord(
            index = index,
            local = local,
            coordinates = coordinates
        )
        playerRecords.add(record)
    }

    private fun group(record: UpdateRecord) {
        record.flag = record.flag shr 1
        if (record.reset) {
            record.flag = 0
            record.coordinates = 0
            record.local = false
            record.reset = false
        }
    }

    private fun walkDir(dx: Int, dy: Int): Int? = when {
        dx == -1 && dy == -1 -> 0
        dx == 0 && dy == -1 -> 1
        dx == 1 && dy == -1 -> 2
        dx == -1 && dy == 0 -> 3
        dx == 1 && dy == 0 -> 4
        dx == -1 && dy == 1 -> 5
        dx == 0 && dy == 1 -> 6
        dx == 1 && dy == 1 -> 7
        else -> null
    }

    private fun runDir(dx: Int, dy: Int): Int? = when {
        dx == -2 && dy == -2 -> 0
        dx == -1 && dy == -2 -> 1
        dx == 0 && dy == -2 -> 2
        dx == 1 && dy == -2 -> 3
        dx == 2 && dy == -2 -> 4
        dx == -2 && dy == -1 -> 5
        dx == 2 && dy == -1 -> 6
        dx == -2 && dy == 0 -> 7
        dx == 2 && dy == 0 -> 8
        dx == -2 && dy == 1 -> 9
        dx == 2 && dy == 1 -> 10
        dx == -2 && dy == 2 -> 11
        dx == -1 && dy == 2 -> 12
        dx == 0 && dy == 2 -> 13
        dx == 1 && dy == 2 -> 14
        dx == 2 && dy == 2 -> 15
        else -> null
    }

    private val UpdateGroup.bit: Int
        get() = when (this) {
            UpdateGroup.Active -> 0
            UpdateGroup.Inactive -> 1
        }

    private val ClientDevice.maskPackets: UpdateMaskPacketMap
        get() = when (this) {
            Device.Desktop -> devicePackets.playerUpdate(Device.Desktop)
            Device.Ios -> devicePackets.playerUpdate(Device.Ios)
            Device.Android -> devicePackets.playerUpdate(Device.Android)
            else -> error("Invalid client device (type=${this::class.simpleName})")
        }
}
