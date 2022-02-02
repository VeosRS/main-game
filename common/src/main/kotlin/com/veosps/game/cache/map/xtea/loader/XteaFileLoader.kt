package com.veosps.game.cache.map.xtea.loader

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.config.models.GameConfig
import com.veosps.game.event.Event
import com.veosps.game.event.EventBus
import com.veosps.game.models.repository.XteaRepository
import java.nio.file.Files

private const val FILE = "xteas.json"
private val logger = InlineLogger()

class XteaLoadEvent : Event

class XteaFileLoader(
    private val config: GameConfig,
    private val mapper: ObjectMapper,
    private val repository: XteaRepository,
    private val eventBus: EventBus
) {

    fun load() {
        val file = config.cachePath.resolve(FILE)
        Files.newBufferedReader(file).use { reader ->
            val list = mapper.readValue(reader, Array<Xteas>::class.java)
            list.forEach { repository.insert(it.key, it.mapSquare) }
            logger.info { "Loaded ${list.size} XTEA keys" }
        }
        eventBus.publish(XteaLoadEvent())
    }
}

private data class Xteas(
    @JsonProperty("mapsquare")
    val mapSquare: Int,
    val key: IntArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Xteas

        if (mapSquare != other.mapSquare) return false
        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mapSquare
        result = 31 * result + key.contentHashCode()
        return result
    }
}
