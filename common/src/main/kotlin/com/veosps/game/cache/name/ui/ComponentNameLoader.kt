package com.veosps.game.cache.name.ui

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.cache.types.ui.ComponentTypeList
import com.veosps.game.config.file.DefaultExtensions
import com.veosps.game.config.file.NamedConfigFileMap
import com.veosps.game.name.NamedTypeLoader
import com.veosps.game.util.UIComponent
import com.veosps.game.util.yamlMapper
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

private val logger = InlineLogger()

@Component
class ComponentNameLoader(
    private val files: NamedConfigFileMap,
    private val names: ComponentNameMap,
    private val types: ComponentTypeList,
    private val interfaces: UserInterfaceNameMap
) : NamedTypeLoader {

    override fun load(directory: Path) {
        val files = files.getValue(DefaultExtensions.COMPONENT_NAMES)
        files.forEach(::loadAliasFile)
        logger.info { "Loaded ${names.size} component type name${if (names.size != 1) "s" else ""}" }
    }

    private fun loadAliasFile(file: Path) {
        Files.newInputStream(file).use { input ->
            val nodes = yamlMapper.readValue(input, Array<NamedComponent>::class.java)
            nodes.forEach { node ->
                val (name, parentName, child) = node
                val parent = interfaces[parentName] ?: error("Interface with name \"$parentName\" not found.")
                val component = UIComponent(parent.id, child)
                val type = types.getOrNull(component.packed) ?: error(
                    "Component type does not exist " +
                        "(component=$parent:$child, file=${file.fileName}, path=${file.toAbsolutePath()})"
                )
                names[name] = type
            }
        }
    }
}

private data class NamedComponent(
    val name: String,
    val parent: String,
    val child: Int
)
