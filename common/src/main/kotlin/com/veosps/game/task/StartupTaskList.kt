package com.veosps.game.task

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.util.BeanScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private val logger = InlineLogger()

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class StartupTaskList internal constructor(
    internal val blocking: MutableList<StartupTask> = mutableListOf(),
    internal val nonBlocking: MutableList<StartupTask> = mutableListOf()
) {

    /* used for dependency injection */
    @Suppress("UNUSED")
    constructor() : this(mutableListOf(), mutableListOf())

    fun registerNonBlocking(block: () -> Unit) = nonBlocking.add(StartupTask(block))

    fun registerBlocking(block: () -> Unit) = blocking.add(StartupTask(block))
}

fun StartupTaskList.launchNonBlocking(scope: CoroutineScope) = runBlocking {
    logger.debug { "Executing non-blocking start up tasks (size=${nonBlocking.size})" }
    val ioJob = scope.launch {
        nonBlocking.forEach {
            launch { it.block() }
        }
    }
    ioJob.join()
}

fun StartupTaskList.launchBlocking() {
    logger.debug { "Executing blocking start up tasks (size=${blocking.size})" }
    blocking.forEach { it.block() }
}
