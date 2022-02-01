package com.veosps.game.models.entities.mob

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.action.ActionBus
import com.veosps.game.cache.types.impl.npc.NpcType
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
import com.veosps.game.models.item.container.ItemContainerMap
import com.veosps.game.models.world.move.MovementSpeed
import com.veosps.game.protocol.message.ServerPacketListener
import com.veosps.game.models.vars.VarpMap
import com.veosps.game.protocol.message.ServerPacket
import com.veosps.game.queue.GameQueueStack

private val logger = InlineLogger()

private const val DEFAULT_ORIENTATION = 0
private const val DEFAULT_RUN_ENERGY = 100.0

sealed class Mob(
    var speed: MovementSpeed = MovementSpeed.Walk,
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
}

inline class PlayerId(val value: Any)

class Player(
    val id: PlayerId,
    val loginName: String,
    val eventBus: EventBus,
    val actionBus: ActionBus,
    override val entity: PlayerEntity,
    var viewport: Viewport = Viewport.ZERO,
    val containers: ItemContainerMap = ItemContainerMap(),
    val varpMap: VarpMap = VarpMap(),
    var runEnergy: Double = DEFAULT_RUN_ENERGY,
    val privileges: MutableList<Privilege> = mutableListOf(),
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
}

class Npc(
    override val entity: NpcEntity = NpcEntity(),
    var type: NpcType,
    var wanderRange: Int = 0
) : Mob() {

    val id: Int
        get() = type.id
}
