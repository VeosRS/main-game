package com.veosps.game.net.channel

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel

private val logger = InlineLogger()

class ClientChannelInitializer : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        logger.debug { "Initialize channel (channel=$channel)" }
    }
}