package com.veosps.game.protocol.update.npc.task

import com.veosps.game.cache.buffer.BitBuf
import com.veosps.game.cache.buffer.toBitBuf
import com.veosps.game.coroutines.IoCoroutineScope
import com.veosps.game.models.Client
import com.veosps.game.models.ClientDevice
import com.veosps.game.models.ClientList
import com.veosps.game.models.entities.mob.Npc
import com.veosps.game.models.entities.mob.NpcList
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.mob.faceDirection
import com.veosps.game.models.map.BuildArea
import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.map.isWithinDistance
import com.veosps.game.models.world.move.Direction
import com.veosps.game.protocol.Device
import com.veosps.game.protocol.packet.server.NpcInfoLargeViewport
import com.veosps.game.protocol.packet.server.NpcInfoSmallViewport
import com.veosps.game.protocol.packet.update.BitMask
import com.veosps.game.protocol.structure.DevicePacketStructureMap
import com.veosps.game.protocol.update.mask.UpdateMask
import com.veosps.game.protocol.update.mask.UpdateMaskPacketMap
import com.veosps.game.protocol.update.task.UpdateTask
import com.veosps.game.util.BeanScope
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private const val MAX_LOCAL_NPC_COUNT = 255
private const val MAX_NPC_ADDITIONS_PER_CYCLE = 40
private const val LARGE_REBUILD_BOUNDARY = 127

private val DIRECTION_ROT = mapOf(
    Direction.SouthWest to 5,
    Direction.South to 6,
    Direction.SouthEast to 7,
    Direction.West to 3,
    Direction.East to 4,
    Direction.NorthWest to 0,
    Direction.North to 1,
    Direction.NorthEast to 2
)

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class NpcUpdateTask(
    private val clientList: ClientList,
    private val npcList: NpcList,
    private val coroutineScope: IoCoroutineScope,
    private val devicePackets: DevicePacketStructureMap
) : UpdateTask {

    override suspend fun execute() {
        val gni = coroutineScope.launch { launchGni() }
        gni.join()
    }

    private fun CoroutineScope.launchGni() = launch {
        clientList.forEach { client ->
            launch {
                val largeViewport = client.player.largeNpcViewport
                val buf = client.gniBuffer(largeViewport)
                val info = viewportInfoPacket(buf, largeViewport)
                client.player.write(info)
            }
        }
    }

    private fun Client.gniBuffer(largeViewport: Boolean): ByteBuf {
        val mainBuf = bufAllocator.buffer()
        val maskBuf = bufAllocator.buffer()
        val masks = device.maskPackets
        mainBuf.getNpcInfo(this, maskBuf, masks, largeViewport)
        mainBuf.writeBytes(maskBuf)
        return mainBuf
    }

    private fun ByteBuf.getNpcInfo(
        client: Client,
        maskBuf: ByteBuf,
        maskPackets: UpdateMaskPacketMap,
        largeViewport: Boolean
    ) {
        val indexes = client.localNpcIndexes
        val bitBuf = toBitBuf()
        indexes.localNpcInfo(client.player, bitBuf, maskBuf, maskPackets, largeViewport)
        indexes.worldNpcInfo(client.player, bitBuf, maskBuf, maskPackets, largeViewport)
        if (maskBuf.writerIndex() > 0) {
            bitBuf.writeBits(value = 0x7FFF, amount = 15)
            bitBuf.byteBuf
        }
    }

    private fun MutableList<Int>.localNpcInfo(
        player: Player,
        bitBuf: BitBuf,
        maskBuf: ByteBuf,
        maskPackets: UpdateMaskPacketMap,
        largeViewport: Boolean
    ) {
        bitBuf.writeLocalNpcCount(size)
        val iterator = iterator()
        while (iterator.hasNext()) {
            val index = iterator.next()
            val localNpc = npcList[index]
            if (localNpc == null || !player.canView(localNpc, largeViewport)) {
                bitBuf.removeLocalNpc()
                iterator.remove()
                continue
            }
            val maskUpdate = localNpc.isMaskUpdateRequired()
            val moveUpdate = localNpc.isMoving()
            if (maskUpdate) {
                maskBuf.writeMaskUpdate(localNpc.entity.updates, maskPackets)
            }
            bitBuf.writeBoolean(maskUpdate || moveUpdate)
            when {
                moveUpdate -> {
                    if (localNpc.displace) {
                        bitBuf.writeTeleport()
                    } else {
                        bitBuf.writeMovement(localNpc)
                        bitBuf.writeBoolean(maskUpdate)
                    }
                }
                maskUpdate -> bitBuf.writeBoolean(false) /* no movement */
            }
        }
    }

    private fun MutableList<Int>.worldNpcInfo(
        player: Player,
        bitBuf: BitBuf,
        maskBuf: ByteBuf,
        maskPackets: UpdateMaskPacketMap,
        largeViewport: Boolean
    ) {
        var added = 0
        for (npc in npcList) {
            if (npc == null) continue
            val addCapacityReached = added >= MAX_NPC_ADDITIONS_PER_CYCLE
            val localCapacityReached = size >= MAX_LOCAL_NPC_COUNT
            if (addCapacityReached || localCapacityReached) {
                break
            }
            if (contains(npc.index) || !player.canView(npc, largeViewport)) {
                continue
            }
            val maskUpdate = npc.isMaskUpdateRequired()
            if (maskUpdate) {
                maskBuf.writeMaskUpdate(npc.entity.updates, maskPackets)
            }
            bitBuf.writeNewLocalNpc(player, npc, largeViewport)
            added++
            add(npc.index)
        }
    }

    private fun BitBuf.writeLocalNpcCount(count: Int) {
        writeBits(value = count, amount = 8)
    }

    private fun BitBuf.removeLocalNpc() {
        writeBits(value = 1, amount = 1)
        writeBits(value = 3, amount = 2)
    }

    private fun BitBuf.writeTeleport() {
        writeBits(value = 3, amount = 2)
    }

    private fun BitBuf.writeMovement(npc: Npc) {
        val steps = npc.movement.nextSteps
        val walkStep = steps.firstOrNull()
        val runStep = if (steps.size > 1) steps[1] else null
        val walkRot = DIRECTION_ROT[walkStep?.dir] ?: 0
        val runRot = DIRECTION_ROT[runStep?.dir]
        val running = runRot != null
        writeBits(value = if (running) 2 else 1, amount = 2)
        writeBits(value = walkRot, amount = 3)
        if (running && runRot != null) {
            writeBits(value = runRot, amount = 3)
        }
    }

    private fun BitBuf.writeNewLocalNpc(player: Player, npc: Npc, largeViewport: Boolean) {
        var diffX = npc.coordinates.x - player.coordinates.x
        var diffY = npc.coordinates.y - player.coordinates.y
        val addition = if (largeViewport) 256 else 32
        if (diffX < 0) {
            diffX += addition
        }
        if (diffY < 0) {
            diffY += addition
        }
        val maskUpdate = npc.isMaskUpdateRequired()
        val npcId = if (npc.entity.transform != -1) npc.entity.transform else npc.id
        val rotation = DIRECTION_ROT.getValue(npc.faceDirection())
        writeBits(value = npc.index, amount = 15)
        writeBits(value = diffX, amount = if (largeViewport) 8 else 5)
        writeBits(value = npcId, amount = 14)
        writeBits(value = rotation, amount = 3)
        writeBoolean(maskUpdate)
        writeBoolean(false) /* not walking */
        writeBits(value = diffY, amount = if (largeViewport) 8 else 5)
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

    private fun ByteBuf.writeMaskBit(
        bitmask: Int,
        handlers: UpdateMaskPacketMap
    ) {
        val mask = BitMask(bitmask)
        val handler = handlers.getValue(mask)
        handler.write(mask, this)
    }

    private fun Player.canView(npc: Npc, largeViewport: Boolean): Boolean {
        return !npc.entity.invisible && npc.coordinates.isWithinView(coordinates, largeViewport)
    }

    private fun Npc.isMoving(): Boolean {
        return movement.nextSteps.isNotEmpty() || displace
    }

    private fun Npc.isMaskUpdateRequired(): Boolean {
        return entity.updates.isNotEmpty()
    }

    private fun Coordinates.isWithinView(coords: Coordinates, largeViewport: Boolean): Boolean {
        val distance = if (largeViewport) LARGE_REBUILD_BOUNDARY else BuildArea.REBUILD_BOUNDARY
        return isWithinDistance(coords, distance - 1)
    }

    private fun viewportInfoPacket(buf: ByteBuf, largeViewport: Boolean) = if (largeViewport) {
        NpcInfoLargeViewport(buf)
    } else {
        NpcInfoSmallViewport(buf)
    }

    private val ClientDevice.maskPackets: UpdateMaskPacketMap
        get() = when (this) {
            Device.Desktop -> devicePackets.npcUpdate(Device.Desktop)
            Device.Ios -> devicePackets.npcUpdate(Device.Ios)
            Device.Android -> devicePackets.npcUpdate(Device.Android)
            else -> error("Invalid client device (type=${this::class.simpleName})")
        }
}
