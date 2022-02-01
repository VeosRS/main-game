package com.veosps.game.cache

import com.displee.cache.CacheLibrary
import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.util.toBuffer
import io.netty.buffer.ByteBuf
import java.nio.file.Path

private val logger = InlineLogger()

/**
 * The master index file id that contains the settings.
 */
const val MASTER_INDEX: Int = 255

class GameCache(
    private val directory: Path,
    private val cache: CacheLibrary = CacheLibrary(directory.toString()),
    private val crcs: MutableList<Int> = mutableListOf()
) {

    val archiveCount: Int
        get() = cache.indices().size - 1

    val archiveCrcs: IntArray
        get() = crcs.toIntArray()

    fun start() {
        cache.index255?.cache()

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

    fun read(archive: Int, group: Int): ByteBuf {
        return when {
            archive == MASTER_INDEX && group != MASTER_INDEX -> cache.index255?.readArchiveSector(group)?.data?.toBuffer()
            else -> cache.index(archive).readArchiveSector(group)?.data?.toBuffer()
        } ?: error("Data was empty for $archive/$group")
    }

    fun uKeys(): ByteArray {
        return cache.generateOldUkeys()
    }

    fun groupIds(archive: Int): List<Int> = cache.index(archive).archives().map { it.id }
}