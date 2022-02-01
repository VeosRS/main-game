package com.veosps.game

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.cache.GameCache
import com.veosps.game.cache.GameCacheGuthix
import com.veosps.game.cache.types.TypeLoader
import com.veosps.game.config.models.GameConfig
import com.veosps.game.coroutines.IoCoroutineScope
import com.veosps.game.coroutines.ioCoroutineScope
import com.veosps.game.net.channel.ClientChannelInitializer
import com.veosps.game.net.handshake.HandshakeDecoder
import com.veosps.game.plugin.kotlin.KotlinPluginLoader
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

private val logger = InlineLogger()

@Component
class GameServer(
    private val beanFactory: BeanFactory,
    private val gameConfig: GameConfig,
    private val gameCache: GameCache,
    private val typeLoaders: List<TypeLoader>,
    private val pluginLoader: KotlinPluginLoader,
) : InitializingBean {

    private fun boot() {
        logger.info { "Starting game server for ${gameConfig.name}..." }

        logger.debug { "Loaded config: $gameConfig" }

        gameCache.start()

        loadCacheTypes(ioCoroutineScope, typeLoaders)

        val plugins = pluginLoader.load()

        bind()

        logger.info { "Loaded ${plugins.size} plugin(s)." }
        logger.info { "Game listening to connections on: ${gameConfig.hostAddress}:${gameConfig.hostPort}" }
    }

    private fun bind() {
        val channelInitializer = ClientChannelInitializer(
            handshakeDecoder = { beanFactory.getBean(HandshakeDecoder::class.java) }
        )

        val bootstrap = ServerBootstrap()
        bootstrap.channel(NioServerSocketChannel::class.java)
        bootstrap.childHandler(channelInitializer)
        bootstrap.group(NioEventLoopGroup(2), NioEventLoopGroup(1))

        val bind = bootstrap.bind(InetSocketAddress(gameConfig.hostAddress, gameConfig.hostPort)).awaitUninterruptibly()

        if (!bind.isSuccess)
            error("Could not bind to game host/port: ${gameConfig.hostAddress}:${gameConfig.hostPort}.")
    }

    override fun afterPropertiesSet() {
        boot()
    }
}

private fun loadCacheTypes(
    ioCoroutineScope: IoCoroutineScope,
    loaders: List<TypeLoader>
) = runBlocking {
    val jobs = mutableListOf<Deferred<Unit>>()
    loaders.forEach {
        jobs.add(ioCoroutineScope.async { it.load() })
    }
    jobs.forEach { it.await() }
}