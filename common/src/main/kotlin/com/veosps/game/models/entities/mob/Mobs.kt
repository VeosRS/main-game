package com.veosps.game.models.entities.mob

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.action.ActionBus
import com.veosps.game.cache.types.npc.NpcType
import com.veosps.game.event.Event
import com.veosps.game.event.EventBus
import com.veosps.game.event.impl.LoginEvent
import com.veosps.game.event.timer.TimerMap
import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.map.Viewport
import com.veosps.game.models.entities.Entity
import com.veosps.game.models.entities.NpcEntity
import com.veosps.game.models.entities.PlayerEntity
import com.veosps.game.models.entities.player.privilege.Privilege
import com.veosps.game.models.entities.player.stat.StatMap
import com.veosps.game.models.item.container.ItemContainer
import com.veosps.game.models.item.container.ItemContainerMap
import com.veosps.game.models.snapshot.Snapshot
import com.veosps.game.models.ui.InterfaceList
import com.veosps.game.models.world.move.MovementSpeed
import com.veosps.game.protocol.message.ServerPacketListener
import com.veosps.game.models.vars.VarpMap
import com.veosps.game.models.world.move.MoveRequest
import com.veosps.game.models.world.move.MovementQueue
import com.veosps.game.protocol.message.ServerPacket
import com.veosps.game.queue.GameQueueStack
import com.veosps.game.queue.QueueType
import java.time.LocalDateTime

private val logger = InlineLogger()

private const val DEFAULT_ORIENTATION = 0
private const val DEFAULT_RUN_ENERGY = 100.0

sealed class Mob(
    val movement: MovementQueue = MovementQueue(),
    var speed: MovementSpeed = MovementSpeed.Walk,
    var orientation: Int = DEFAULT_ORIENTATION,
    var displace: Boolean = false,
    var lastSpeed: MovementSpeed? = null,
    var moveRequest: MoveRequest? = null,
    val stats: StatMap = StatMap(),
    val timers: TimerMap = TimerMap(),
    val queueStack: GameQueueStack = GameQueueStack()
) {

    abstract val entity: Entity

    var index: Int
        get() = entity.index
        set(value) {
            entity.index = value
        }

    var coordinates: Coordinates
        get() = entity.coordinates
        set(value) {
            entity.coordinates = value
        }

    fun weakQueue(block: suspend () -> Unit) = queueStack.queue(QueueType.Weak, block)

    fun normalQueue(block: suspend () -> Unit) = queueStack.queue(QueueType.Normal, block)

    fun strongQueue(block: suspend () -> Unit) = queueStack.queue(QueueType.Strong, block)

    fun clearQueues() = queueStack.clear()

    fun displace(coords: Coordinates) {
        this.coordinates = coords
        this.displace = true
    }

    fun stopMovement() {
        movement.clear()
        movement.nextSteps.clear()
    }
}

@Suppress("INLINE_CLASS_DEPRECATED")
inline class PlayerId(val value: Any)

class Player(
    val id: PlayerId,
    val loginName: String,
    val eventBus: EventBus,
    val actionBus: ActionBus,
    override val entity: PlayerEntity,
    var snapshot: Snapshot = Snapshot.INITIAL,
    var viewport: Viewport = Viewport.ZERO,
    val inventory: ItemContainer = ItemContainer(),
    val equipment: ItemContainer = ItemContainer(),
    val bank: ItemContainer = ItemContainer(),
    val containers: ItemContainerMap = ItemContainerMap(),
    val ui: InterfaceList = InterfaceList(),
    val varpMap: VarpMap = VarpMap(),
    var runEnergy: Double = DEFAULT_RUN_ENERGY,
    val privileges: MutableList<Privilege> = mutableListOf(),
    var largeNpcViewport: Boolean = false,
    private val messageListeners: List<ServerPacketListener> = mutableListOf()
) : Mob() {

    val username: String
        get() = entity.username

    fun login() {
        eventBus.publish(LoginEvent(this, LoginEvent.Priority.High))
        eventBus.publish(LoginEvent(this, LoginEvent.Priority.Normal))
        eventBus.publish(LoginEvent(this, LoginEvent.Priority.Low))
    }

    fun write(packet: ServerPacket) {
        messageListeners.forEach { it.write(packet) }
    }

    fun flush() {
        messageListeners.forEach { it.flush() }
    }

    fun addPrivilege(privilege: Privilege, primary: Boolean = true) {
        if (primary) {
            privileges.remove(privilege)
            privileges.add(0, privilege)
        } else if (!privileges.contains(privilege)) {
            privileges.add(privilege)
        }
    }

    fun hasPrivilege(privilege: Privilege): Boolean {
        return privileges.contains(privilege)
    }

    fun snapshot() = Snapshot(
        timestamp = LocalDateTime.now(),
        coords = coordinates,
        entity = entity.copy(),
        stats = stats.copy(),
        varps = varpMap.copy(),
        containers = containers.copyAutoUpdateOnly()
    )

    fun info(message: () -> String) {
        logger.info { "$username: ${message()}" }
    }

    fun warn(message: () -> String) {
        logger.warn { "$username: ${message()}" }
    }

    fun trace(message: () -> String) {
        logger.trace { "$username: ${message()}" }
    }

    fun debug(message: () -> String) {
        logger.debug { "$username: ${message()}" }
    }

    inline fun <reified T : Event> submitEvent(event: T) {
        queueStack.submitEvent(event)
        eventBus.publish(event)
    }
}

class Npc(
    override val entity: NpcEntity = NpcEntity(),
    var type: NpcType,
    var wanderRange: Int = 0
) : Mob() {

    val id: Int
        get() = type.id
}
