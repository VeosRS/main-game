package com.veosps.game.models.ui.gameframe

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.cache.name.ui.ComponentNameMap
import com.veosps.game.cache.name.ui.UserInterfaceNameMap
import org.springframework.stereotype.Component

private val logger = InlineLogger()

@Component
class GameFrameList(
    private val interfaces: UserInterfaceNameMap,
    private val components: ComponentNameMap,
    private val frames: MutableMap<GameFrameType, GameFrame> = mutableMapOf()
) : Map<GameFrameType, GameFrame> by frames {

    fun register(frame: GameFrame) {
        check(!frames.containsKey(frame.type)) { "GameFrame with type already exists (type=${frame.type})" }
        logger.debug { "Register GameFrame (frame=$frame)" }
        frames[frame.type] = frame
    }

    fun register(init: GameFrameBuilder.() -> Unit) {
        val builder = GameFrameBuilder().apply(init)
        val gameFrame = builder.build(interfaces, components)
        register(gameFrame)
    }
}