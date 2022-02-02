package com.veosps.game.cache.name.vars

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.config.file.DefaultExtensions
import com.veosps.game.config.file.NamedConfigFileMap
import com.veosps.game.models.vars.type.VarbitTypeList
import com.veosps.game.models.vars.type.VarpTypeList
import com.veosps.game.name.NamedTypeLoader
import com.veosps.game.util.BeanScope
import com.veosps.game.util.yamlMapper
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

private val logger = InlineLogger()

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class VarpNameLoader(
    private val files: NamedConfigFileMap,
    private val names: VarpNameMap,
    private val types: VarpTypeList
) : NamedTypeLoader {

    override fun load(directory: Path) {
        val files = files.getValue(DefaultExtensions.VARP_NAMES)
        files.forEach(::loadAliasFile)
        logger.info { "Loaded ${names.size} varp type name${if (names.size != 1) "s" else ""}" }
    }

    private fun loadAliasFile(file: Path) {
        Files.newInputStream(file).use { input ->
            val nodes = yamlMapper.readValue(input, LinkedHashMap<String, Int>()::class.java)
            nodes.forEach { node ->
                val name = node.key
                val id = node.value
                val type = types.getOrNull(id) ?: error(
                    "Varp type does not exist (varp=$id, file=${file.fileName}, path=${file.toAbsolutePath()})"
                )
                names[name] = type
            }
        }
    }
}

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class VarbitNameLoader(
    private val files: NamedConfigFileMap,
    private val names: VarbitNameMap,
    private val types: VarbitTypeList
) : NamedTypeLoader {

    override fun load(directory: Path) {
        val files = files.getValue(DefaultExtensions.VARBIT_NAMES)
        files.forEach(::loadAliasFile)
        logger.info { "Loaded ${names.size} varbit type name${if (names.size != 1) "s" else ""}" }
    }

    private fun loadAliasFile(file: Path) {
        Files.newInputStream(file).use { input ->
            val nodes = yamlMapper.readValue(input, LinkedHashMap<String, Int>()::class.java)
            nodes.forEach { node ->
                val name = node.key
                val id = node.value
                val type = types.getOrNull(id) ?: error(
                    "Varbit type does not exist (varbit=$id, file=${file.fileName}, path=${file.toAbsolutePath()})"
                )
                names[name] = type
            }
        }
    }
}