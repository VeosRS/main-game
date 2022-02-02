package com.veosps.game.cache.name.ui

import com.veosps.game.cache.types.ui.InterfaceTypeList
import com.veosps.game.name.NamedTypeLoader
import com.veosps.game.config.file.DefaultExtensions
import com.veosps.game.config.file.NamedConfigFileMap
import com.veosps.game.util.yamlMapper
import java.nio.file.Files
import java.nio.file.Path
import org.springframework.stereotype.Component
import com.github.michaelbull.logging.InlineLogger

private val logger = InlineLogger()

@Component
class UserInterfaceNameLoader(
    private val files: NamedConfigFileMap,
    private val names: UserInterfaceNameMap,
    private val types: InterfaceTypeList,
    private val componentLoader: ComponentNameLoader
) : NamedTypeLoader {

    override fun load(directory: Path) {
        val files = files.getValue(DefaultExtensions.INTERFACE_NAMES)
        files.forEach(::loadAliasFile)
        logger.info { "Loaded ${names.size} interface type name${if (names.size != 1) "s" else ""}" }
        componentLoader.load(directory)
    }

    private fun loadAliasFile(file: Path): Int {
        var count = 0
        Files.newInputStream(file).use { input ->
            val nodes = yamlMapper.readValue(input, LinkedHashMap<String, Int>()::class.java)
            nodes.forEach { node ->
                val name = node.key
                val interfaceId = node.value
                val type = types.getOrNull(interfaceId) ?: error(
                    "Interface type does not exist " +
                            "(interface=$interfaceId, file=${file.fileName}, path=${file.toAbsolutePath()})"
                )
                names[name] = type
                count++
            }
        }
        return count
    }
}
