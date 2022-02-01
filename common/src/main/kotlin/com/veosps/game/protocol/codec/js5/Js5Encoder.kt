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
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

private val logger = InlineLogger()

@ChannelHandler.Sharable
object Js5Encoder : MessageToByteEncoder<Js5Response>() {

    private const val BLOCK_LENGTH = 512

    override fun encode(ctx: ChannelHandlerContext, msg: Js5Response, out: ByteBuf) {
        logger.trace { "Encode JS5 response (message=$msg, channel=${ctx.channel()})" }
        out.writeByte(msg.archive)
        out.writeShort(msg.group)
        out.writeByte(msg.compressionType)
        out.writeInt(msg.compressedLength)
        msg.data.forEach { byte ->
            if (out.writerIndex() % BLOCK_LENGTH == 0) {
                /* Notify the client that we're done writing a single block */
                out.writeByte(-1)
            }
            out.writeByte(byte.toInt())
        }
    }
}
