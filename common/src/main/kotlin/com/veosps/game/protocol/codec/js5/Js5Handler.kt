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
package com.veosps.game.protocol.codec.js5

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.protocol.codec.ResponseType
import com.veosps.game.protocol.codec.exceptionCaught
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private val logger = InlineLogger()

@Component
class Js5Handler @Autowired constructor(
    private val dispatcher: Js5Dispatcher
) : ChannelInboundHandlerAdapter() {

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        logger.trace { "Channel registered (channel=${ctx.channel()})" }
        super.handlerAdded(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        logger.trace { "Channel unregistered (channel=${ctx.channel()})" }
        super.channelUnregistered(ctx)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is Js5Request -> {
                logger.trace { "Add JS5 request (request=$msg, channel=${ctx.channel()})" }
                dispatcher.add(ctx.channel(), msg)
            }
            is ResponseType -> ctx.channel().writeAndFlush(msg)
            else -> {
                logger.error { "Invalid message type (message=$msg)" }
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        ctx.exceptionCaught(cause)
    }
}
