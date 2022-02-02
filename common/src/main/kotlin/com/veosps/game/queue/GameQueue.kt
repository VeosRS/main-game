package com.veosps.game.queue

import com.veosps.game.coroutines.GameCoroutineTask

@Suppress("INLINE_CLASS_DEPRECATED")
inline class GameQueue(val task: GameCoroutineTask)

@Suppress("INLINE_CLASS_DEPRECATED")
inline class GameQueueBlock(val block: suspend () -> Unit)

sealed class QueueType {
    object Weak : QueueType()
    object Normal : QueueType()
    object Strong : QueueType()
}
