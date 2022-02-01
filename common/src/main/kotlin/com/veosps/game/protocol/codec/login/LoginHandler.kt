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
package com.veosps.game.protocol.codec.login

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.protocol.codec.ResponseType
import com.veosps.game.protocol.codec.account.AccountDispatcher
import com.veosps.game.protocol.codec.exceptionCaught
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

private val logger = InlineLogger()

class LoginHandler(
    private val dispatcher: AccountDispatcher
) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is LoginRequest -> dispatcher.register(msg)
            is ResponseType -> ctx.channel().writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE)
            else -> logger.error { "Invalid message type (message=$msg)" }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        ctx.exceptionCaught(cause)
    }
}
