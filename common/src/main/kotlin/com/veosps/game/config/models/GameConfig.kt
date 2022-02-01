package com.veosps.game.config.models

import com.veosps.game.models.Position
import com.veosps.game.util.toPath
import java.nio.file.Path

data class GameConfig(
    val name: String,
    val majorRevision: Int,
    val minorRevision: Int,
    val spawnPosition: Position,
    val environment: String,
    val hostAddress: String,
    val hostPort: Int,
    val dataPath: String
) {

    val cachePath: Path
        get() = dataPath.toPath().resolve("cache")

    val rsaPath: Path
        get() = dataPath.toPath().resolve(Path.of("rsa", "key.pem"))
}

sealed class GameEnvironment {
    object Development : GameEnvironment()
    object Staging : GameEnvironment()
    object Production : GameEnvironment()

    override fun toString(): String = javaClass.simpleName
}