package com.veosps.game.name

import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
interface NamedTypeLoader {
    fun load(directory: Path)
}
