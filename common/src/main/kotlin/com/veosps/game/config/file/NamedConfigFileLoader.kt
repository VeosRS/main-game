package com.veosps.game.config.file

import com.veosps.game.config.models.GameConfig
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.nio.file.Files

@Component
class NamedConfigFileLoader(
    private val config: GameConfig
) {

    @Bean
    @Scope(BeanScope.SCOPE_SINGLETON)
    fun loadPluginConfigs() = NamedConfigFileMap().apply {
        DefaultExtensions.ALL.forEach { this += it }

        val files = Files.list(config.pluginConfigPath)
        files.forEach { loadDirectory(it) }
    }
}