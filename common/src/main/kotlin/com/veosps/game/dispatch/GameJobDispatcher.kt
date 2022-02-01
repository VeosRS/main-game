package com.veosps.game.dispatch

import com.github.michaelbull.logging.InlineLogger
import org.springframework.stereotype.Component

private val logger = InlineLogger()

private class GameDispatchJob(
    val block: () -> Unit,
    val singleExecute: Boolean
)

@Component
class GameJobDispatcher private constructor(
    private val jobs: MutableList<GameDispatchJob>
) : List<GameDispatchJob> by jobs {

    constructor() : this(mutableListOf())

    fun schedule(block: () -> Unit) {
        val job = GameDispatchJob(block = block, singleExecute = false)
        jobs.add(job)
        logger.debug { "Schedule continuous dispatcher job (totalJobs=${jobs.size})" }
    }

    fun execute(block: () -> Unit) {
        val job = GameDispatchJob(block = block, singleExecute = true)
        jobs.add(job)
        logger.debug { "Schedule one-time dispatcher job (totalJobs=${jobs.size})" }
    }

    internal fun executeAll() {
        jobs.forEach { it.block() }
        jobs.removeIf { it.singleExecute }
    }
}
