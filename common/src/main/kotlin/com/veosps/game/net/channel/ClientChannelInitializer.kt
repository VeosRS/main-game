/*
Copyright (c) 2020 RS Mod

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/
package com.veosps.game.net.channel

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.traffic.GlobalTrafficShapingHandler
import io.netty.handler.traffic.TrafficCounter
import java.util.concurrent.Executors

private val logger = InlineLogger()

class ClientChannelInitializer(
    private val handshakeDecoder: () -> ChannelHandler
) : ChannelInitializer<SocketChannel>() {

    private val trafficHandler = GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000)

    val trafficStats: TrafficCounter
        get() = trafficHandler.trafficCounter()

    override fun initChannel(channel: SocketChannel) {
        logger.debug { "Initialize channel (channel=$channel)" }
        channel.pipeline().addLast(trafficHandler, handshakeDecoder())
    }
}