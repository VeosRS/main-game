package com.veosps.game.plugins.fundamentals.protocol

import com.veosps.game.cache.map.xtea.loader.XteaFileLoader
import com.veosps.game.task.StartupTaskList

val tasks: StartupTaskList by inject()
val loader: XteaFileLoader by inject()

tasks.registerNonBlocking {
    loader.load()
}
