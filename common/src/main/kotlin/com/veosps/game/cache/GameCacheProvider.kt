package com.veosps.game.cache

import com.veosps.game.config.models.GameConfig
import com.veosps.game.util.BeanScope
import io.guthix.js5.Js5Cache
import io.guthix.js5.container.disk.Js5DiskStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.nio.file.Files

@Component
class GameCacheProvider(
    private val serverConfig: GameConfig
) {

    @Bean
    @Scope(BeanScope.SCOPE_SINGLETON)
    fun getCache(): GameCache {
        val path = serverConfig.cachePath.resolve("packed")
        if (!Files.isDirectory(path)) {
            error("Cache directory does not exist: ${path.toAbsolutePath()}")
        }
        val diskStore = Js5DiskStore.open(path)
        val cache = Js5Cache(diskStore)
        return GameCache(path, diskStore, cache)
    }
}