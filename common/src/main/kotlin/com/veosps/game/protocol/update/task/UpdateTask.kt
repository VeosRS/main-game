package com.veosps.game.protocol.update.task

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

private val logger = InlineLogger()

interface UpdateTask {
    suspend fun execute()
}

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class UpdateTaskList(
    private val tasks: MutableList<UpdateTask>
) : List<UpdateTask> by tasks {

    fun register(init: UpdateTaskBuilder.() -> Unit) {
        UpdateTaskBuilder(mutableListOf()).apply(init)
    }
}

@DslMarker
private annotation class TaskBuilderDslMarker

@TaskBuilderDslMarker
class UpdateTaskBuilder(private val tasks: MutableList<UpdateTask>) {

    operator fun <T : UpdateTask> T.unaryMinus() {
        logger.debug { "Append update task to list (${tasks.size}) (task=${this::class.simpleName})" }
        tasks.add(this)
    }
}
