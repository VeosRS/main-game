package com.veosps.game.cache

import com.veosps.game.config.models.GameConfig
import com.veosps.game.util.BeanScope
import com.veosps.game.util.pathOf
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
class GameCacheProvider(
    private val serverConfig: GameConfig
) {

    @Bean
    @Scope(BeanScope.SCOPE_SINGLETON)
    fun getCache(): GameCache {
        return GameCache(serverConfig.cachePath.resolve("packed"))
    }
}