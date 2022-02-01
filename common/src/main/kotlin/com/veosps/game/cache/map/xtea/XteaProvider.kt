package com.veosps.game.cache.map.xtea

import com.veosps.game.cache.map.xtea.loader.XteaFileLoader
import com.veosps.game.cache.map.xtea.repository.XteaInMemoryRepository
import com.veosps.game.config.models.GameConfig
import com.veosps.game.event.EventBus
import com.veosps.game.models.repository.XteaRepository
import com.veosps.game.util.jsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class XteaProvider(
    private val gameConfig: GameConfig,
    private val eventBus: EventBus
) {

    @Bean
    fun xteaRepository(): XteaRepository {
        return XteaInMemoryRepository()
    }

    @Bean
    fun xteaLoader(xteaRepository: XteaRepository): XteaFileLoader {
        return XteaFileLoader(gameConfig, jsonMapper, xteaRepository, eventBus)
    }
}