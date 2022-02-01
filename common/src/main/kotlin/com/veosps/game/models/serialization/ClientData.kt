package com.veosps.game.models.serialization

import com.veosps.game.models.Client
import kotlin.reflect.KClass

interface ClientData

interface ClientDataMapper<T : ClientData> {

    val type: KClass<T>

    fun deserialize(request: ClientDeserializeRequest, data: T): ClientDeserializeResponse

    fun serialize(client: Client): T

    fun newClient(request: ClientDeserializeRequest): Client
}
