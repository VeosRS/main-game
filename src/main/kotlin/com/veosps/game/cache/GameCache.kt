package com.veosps.game.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.index.archive.file.File
import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.util.toBuffer
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.nio.file.Path

private val logger = InlineLogger()

class GameCache(
    private val directory: Path,
    private val cache: CacheLibrary = CacheLibrary(directory.toString()),
    private val crcs: MutableList<Int> = mutableListOf()
) {

    fun start() {
        cache.indices().forEach {
            logger.trace { "Adding CRC for archive '${it.info}': ${it.crc}..." }
            crcs.add(it.crc)
        }

        logger.info { "Successfully prepared game cache and loaded the CRCs..." }
    }

    fun groups(archive: Int, group: Int): Map<Int, ByteBuf> {
        val files = cache.index(archive).archive(group)?.files

        return files?.mapValues {
            it.value.data?.toBuffer() ?: error("No data for file ${it.value.id}.")
        } ?: error("No files were found for group $group in archive $archive.")
    }
}