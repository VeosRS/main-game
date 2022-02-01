package com.veosps.game.models

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.MoreObjects
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.protocol.message.ClientPacket
import com.veosps.game.protocol.message.ClientPacketMessage
import com.veosps.game.protocol.update.record.UpdateRecord
import com.veosps.game.util.BeanScope
import io.netty.buffer.ByteBufAllocator
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

private val logger = InlineLogger()

class Client(
    val player: Player,
    val device: ClientDevice,
    val machine: ClientMachine,
    var settings: ClientSettings,
    val encryptedPass: String,
    val loginXteas: IntArray,
    val bufAllocator: ByteBufAllocator,
    val playerRecords: MutableList<UpdateRecord> = mutableListOf(),
    val localNpcIndexes: MutableList<Int> = mutableListOf(),
    val pendingPackets: Queue<ClientPacketMessage<out ClientPacket>> = LinkedList()
) {

    override fun toString(): String = MoreObjects
        .toStringHelper(this)
        .add("player", player)
        .add("machine", machine)
        .add("settings", settings)
        .toString()
}

sealed class OperatingSystem {
    object Windows : OperatingSystem()
    object Mac : OperatingSystem()
    object Linux : OperatingSystem()
    object Other : OperatingSystem()
    override fun toString(): String = javaClass.simpleName
}

sealed class JavaVendor {
    object Sun : JavaVendor()
    object Microsoft : JavaVendor()
    object Apple : JavaVendor()
    object Other : JavaVendor()
    object Oracle : JavaVendor()
    override fun toString(): String = javaClass.simpleName
}

interface ClientDevice

data class ClientSettings(
    val width: Int,
    val height: Int,
    val flags: Int
)

data class ClientMachine(
    val operatingSystem: OperatingSystem,
    val is64Bit: Boolean,
    val osVersion: Int,
    val javaVendor: JavaVendor,
    val javaVersion: JavaVersion,
    val maxMemory: Int,
    val cpuCount: Int
)

data class JavaVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
)

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class ClientList(
    private val active: MutableList<Client> = mutableListOf()
) : List<Client> by active {

    fun register(client: Client) {
        if (active.any { it.player.id == client.player.id }) {
            logger.error { "Client is already registered (player=${client.player})" }
            return
        }
        active.add(client)
    }

    fun remove(client: Client) {
        if (active.none { it.player.id == client.player.id }) {
            logger.error { "Client is not registered (player=${client.player})" }
            return
        }
        active.remove(client)
    }
}