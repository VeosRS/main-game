package com.veosps.game.models.item.container

import com.github.michaelbull.logging.InlineLogger
import org.springframework.stereotype.Component

private val logger = InlineLogger()

@Component
class ItemContainerKeyMap(
    private val containerKeys: MutableMap<String, ItemContainerKey> = mutableMapOf()
) : Map<String, ItemContainerKey> by containerKeys {

    fun register(key: ItemContainerKey) {
        check(!containerKeys.containsKey(key.name)) { "Container with key \"${key.name}\" already exists." }
        logger.debug { "Register item container key (key=$key)" }
        containerKeys[key.name] = key
    }
}
