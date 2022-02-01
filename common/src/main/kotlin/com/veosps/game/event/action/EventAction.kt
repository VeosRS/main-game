package com.veosps.game.event.action

import com.veosps.game.event.Event

class EventAction<T : Event>(
    val where: (T).() -> Boolean,
    val then: (T).() -> Unit
)
