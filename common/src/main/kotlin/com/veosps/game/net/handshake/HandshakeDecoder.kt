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
package com.veosps.game.net.handshake

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.util.BeanScope
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private val logger = InlineLogger()

@Component
class HandshakeDecoder(
    private val handlers: HandshakeHandlerMap
) : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, buffer: ByteBuf, out: MutableList<Any>) {
        val opcode = buffer.readByte().toInt()
        val handler = handlers[opcode]

        if (handler == null) {
            ctx.disconnect()
            logger.error { "Handler not found for handshake (opcode=$opcode)" }
            return
        }

        val decoder = handler.decoder
        val encoder = handler.encoder
        val adapter = handler.adapter
        val response = handler.response

        ctx.pipeline().replace(this, decoder.name, decoder.provider())
        ctx.pipeline().addLast(encoder.name, encoder.provider())
        ctx.pipeline().addLast(adapter.name, adapter.provider())
        ctx.pipeline().addLast(response.name, response.provider())
    }
}