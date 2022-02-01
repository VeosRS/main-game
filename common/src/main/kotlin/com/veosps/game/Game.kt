package com.veosps.game

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.coroutines.GameCoroutineScope
import com.veosps.game.dispatch.GameJobDispatcher
import com.veosps.game.event.EventBus
import com.veosps.game.event.impl.PlayerTimerTrigger
import com.veosps.game.models.Client
import com.veosps.game.models.ClientList
import com.veosps.game.models.entities.mob.Mob
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.mob.PlayerList
import com.veosps.game.models.world.World
import com.veosps.game.protocol.update.task.UpdateTaskList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

private val logger = InlineLogger()

sealed class GameState {
    object Inactive : GameState()
    object Active : GameState()
    object ShutDown : GameState()
}

@Component
class Game(
    private val coroutineScope: GameCoroutineScope,
    private val jobDispatcher: GameJobDispatcher,
    private val updateTaskList: UpdateTaskList,
    private val playerList: PlayerList,
    private val clientList: ClientList,
    private val eventBus: EventBus,
    private val world: World
) {

    var state: GameState = GameState.Inactive

    private var excessCycleNanos = 0L

    fun start() {
        if (state != GameState.Inactive) {
            error("::start has already been called.")
        }
        val delay = 600
        state = GameState.Active
        coroutineScope.start(delay.toLong())
    }

    private fun CoroutineScope.start(delay: Long) = launch {
        while (state != GameState.ShutDown && isActive) {
            val elapsedNanos = measureNanoTime { gameLogic() } + excessCycleNanos
            val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)
            val overdue = elapsedMillis > delay
            val sleepTime = if (overdue) {
                val elapsedCycleCount = elapsedMillis / delay
                val upcomingCycleDelay = (elapsedCycleCount + 1) * delay
                upcomingCycleDelay - elapsedMillis
            } else {
                delay - elapsedMillis
            }
            if (overdue) logger.error { "Cycle took too long (elapsed=${elapsedMillis}ms, sleep=${sleepTime}ms)" }
            excessCycleNanos = elapsedNanos - TimeUnit.MILLISECONDS.toNanos(elapsedMillis)
            delay(sleepTime)
        }
    }

    private suspend fun gameLogic() {
        clientList.forEach { it.pollActions(25) }
        playerList.forEach { it?.cycle() }
        //npcList.forEach { it?.cycle(eventBus) }
        world.queueList.cycle()
        jobDispatcher.executeAll()
        updateTaskList.forEach { it.execute() }
        playerList.forEach { it?.flush() }
    }
}

private fun Client.pollActions(iterations: Int) {
    for (i in 0 until iterations) {
        val message = pendingPackets.poll() ?: break
        val handler = message.handler
        val packet = message.packet
        try {
            handler.handle(this, player, packet)
        } catch (t: Throwable) {
            logger.error(t) {
                "Action handler process error (packet=${packet::class.simpleName}, " +
                        "handler=${handler::class.simpleName}, player=$player)"
            }
        }
    }
}

private suspend fun Player.cycle() {
    queueCycle()
    timerCycle()
}

private suspend fun Mob.queueCycle() {
    /* flag whether a new queue should be polled this cycle */
    val pollQueue = queueStack.idle
    try {
        queueStack.processCurrent()
    } catch (t: Throwable) {
        queueStack.discardCurrent()
        logger.error(t) { "Queue process error ($this)" }
    }
    if (pollQueue) {
        try {
            queueStack.pollPending()
        } catch (t: Throwable) {
            logger.error(t) { "Queue poll error ($this)" }
        }
    }
}

private fun Player.timerCycle() {
    if (timers.isEmpty()) return
    val iterator = timers.iterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        val key = entry.key
        val cycles = entry.value
        if (cycles > 0) {
            timers.decrement(key)
            continue
        }
        try {
            val event = PlayerTimerTrigger(this, key)
            eventBus.publish(event)
            /* if the timer was not re-set after event we remove it */
            if (timers.isNotActive(key)) {
                iterator.remove()
            }
        } catch (t: Throwable) {
            iterator.remove()
            logger.error(t) { "Timer event error ($this)" }
        }
    }
}