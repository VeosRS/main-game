package com.veosps.game.event.impl

import com.veosps.game.event.Event
import com.veosps.game.models.Client

data class ClientRegister(
    val client: Client
) : Event

data class ClientUnregister(
    val client: Client
) : Event
