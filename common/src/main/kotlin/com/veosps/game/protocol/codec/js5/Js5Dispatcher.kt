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
import com.veosps.game.cache.GameCache
import io.guthix.js5.container.Js5Store
import io.netty.channel.Channel
import org.springframework.stereotype.Component

private val logger = InlineLogger()

@Component
class Js5Dispatcher(
    private val cache: GameCache,
) {
    private val responses: MutableMap<Js5Request, Js5Response> = mutableMapOf()

    fun cacheResponses() {
        cacheResponse(Js5Store.MASTER_INDEX, Js5Store.MASTER_INDEX)

        for (i in 0 until cache.archiveCount) {
            cacheResponse(Js5Store.MASTER_INDEX, i)
        }

        for (i in 0 until cache.archiveCount) {
            val groups = cache.groupIds(i)
            groups.forEach { group ->
                cacheResponse(i, group)
            }
        }

        logger.debug { "Cached JS5 request responses (total=${responses.size})" }
    }

    fun add(channel: Channel, request: Js5Request) {
        val response = request.response()
        logger.trace { "Add JS5 request (request=$request, response=$response, channel=$channel)" }
        channel.writeAndFlush(response)
    }

    private fun Js5Request.response(): Js5Response {
        if (responses.isEmpty()) {
            error("::cacheResponses should be called on server startup.")
        }
        val cachedRequest = Js5Request(archive, group, urgent = false)
        return responses[cachedRequest] ?: error("Js5 request was not cached on startup (request=$this)")
    }

    private fun response(archive: Int, group: Int): Js5Response {
        val data = cache.read(archive, group)

        val compressionType = data.readUnsignedByte().toInt()
        val compressedLength = data.readInt()
        val array = ByteArray(data.writerIndex() - Byte.SIZE_BYTES - Int.SIZE_BYTES)
        data.readBytes(array)

        return Js5Response(
            archive = archive,
            group = group,
            compressionType = compressionType,
            compressedLength = compressedLength,
            data = array
        )
    }

    private fun cacheResponse(archive: Int, group: Int) {
        val request = Js5Request(archive, group, urgent = false)
        val response = response(archive, group)
        logger.trace { "Cache Js5 request (request=$request, response=$response)" }
        responses[request] = response
    }
}
