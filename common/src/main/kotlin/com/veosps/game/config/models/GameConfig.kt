package com.veosps.game.config.models

import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.map.CoordinatesWrapper
import com.veosps.game.models.map.wrap
import com.veosps.game.util.resolve
import com.veosps.game.util.toPath
import java.nio.file.Path

data class GameConfig(
    val name: String,
    val majorRevision: Int,
    val minorRevision: Int = 1,
    val spawnPosition: CoordinatesWrapper = Coordinates(3235, 3219).wrap(),
    val environment: String = "dev",
    val hostAddress: String = "127.0.0.1",
    val hostPort: Int,
    val dataPath: String,
    val pluginPath: String = "./data/plugins",
) {

    val env: GameEnvironment
        get() = when (environment) {
            "dev" -> GameEnvironment.Development
            "staging" -> GameEnvironment.Staging
            "prod" -> GameEnvironment.Production
            else -> error("Unknown environment: $environment")
        }

    val cachePath: Path
        get() = dataPath.toPath().resolve("cache")

    val rsaPath: Path
        get() = dataPath.toPath().resolve(Path.of("rsa", "key.pem"))

    val pluginConfigPath: Path
        get() = pluginPath.resolve("resources")
}

sealed class GameEnvironment {
    object Development : GameEnvironment()
    object Staging : GameEnvironment()
    object Production : GameEnvironment()

    override fun toString(): String = javaClass.simpleName
}