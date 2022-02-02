package com.veosps.game.coroutines

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class IoCoroutineScope(
    override val coroutineContext: CoroutineDispatcher
) : CoroutineScope by CoroutineScope(coroutineContext)

class GameCoroutineScope(
    override val coroutineContext: CoroutineContext
) : CoroutineScope by CoroutineScope(coroutineContext)

class GameCoroutineDispatcher(
    private val executor: GameExecutor
) {
    fun get() = executor.asCoroutineDispatcher()
}

class GameExecutor(
    private val executor: Executor
) : Executor by executor

@Component
class CoroutineProvider {

    @Bean
    fun ioCoroutineScope(): IoCoroutineScope {
        return IoCoroutineScope(Dispatchers.IO)
    }

    @Bean
    fun gameCoroutineScope(): GameCoroutineScope {
        val factory = ThreadFactoryBuilder()
            .setDaemon(false)
            .setNameFormat("GameExecutor")
            .build()
        val executor = Executors.newSingleThreadExecutor(factory)

        return GameCoroutineScope(GameCoroutineDispatcher(GameExecutor(executor)).get())
    }
}