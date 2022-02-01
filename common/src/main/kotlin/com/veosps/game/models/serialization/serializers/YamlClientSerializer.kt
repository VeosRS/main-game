package com.veosps.game.models.serialization.serializers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.config.models.GameConfig
import com.veosps.game.models.Client
import com.veosps.game.models.serialization.*
import com.veosps.game.util.resolve
import java.nio.file.Files
import java.nio.file.Path

private val logger = InlineLogger()

class YamlClientSerializer<T : ClientData>(
    private val config: GameConfig,
    private val objectMapper: ObjectMapper,
    private val dataMapper: ClientDataMapper<T>
) : ClientSerializer {

    private val savePath: Path
        get() = config.dataPath.resolve(SAVE_FOLDER)

    override fun deserialize(request: ClientDeserializeRequest): ClientDeserializeResponse {
        val path = request.loginName.savePath()
        if (!Files.exists(path)) {
            val client = dataMapper.newClient(request)
            return ClientDeserializeResponse.Success(client, newAccount = true)
        }
        Files.newBufferedReader(path).use { reader ->
            return try {
                val data = objectMapper.readValue(reader, dataMapper.type.java)
                dataMapper.deserialize(request, data)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error when trying to deserialize client " +
                            "(name=${request.loginName}, file=${path.toAbsolutePath()})"
                }
                ClientDeserializeResponse.ReadError
            }
        }
    }

    override fun serialize(client: Client) {
        val mapped = dataMapper.serialize(client)
        val path = client.player.loginName.savePath()
        Files.newBufferedWriter(path).use { writer ->
            objectMapper.writeValue(writer, mapped)
        }
    }

    private fun String.savePath(): Path {
        return savePath.resolve("$this.yml")
    }

    companion object {
        private const val SAVE_FOLDER = "saves"
    }
}