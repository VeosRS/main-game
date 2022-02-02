package com.veosps.game.plugins.fundamentals.protocol.update

import com.veosps.game.protocol.update.npc.task.NpcPostUpdateTask
import com.veosps.game.protocol.update.task.UpdateTaskList
import com.veosps.game.protocol.update.npc.task.NpcPreUpdateTask
import com.veosps.game.protocol.update.npc.task.NpcUpdateTask
import com.veosps.game.protocol.update.player.task.PathFinderTask
import com.veosps.game.protocol.update.player.task.PlayerPostUpdateTask
import com.veosps.game.protocol.update.player.task.PlayerPreUpdateTask
import com.veosps.game.protocol.update.player.task.PlayerUpdateTask

val npcPreUpdateTask: NpcPreUpdateTask by inject()
val npcUpdateTask: NpcUpdateTask by inject()
val npcPostUpdateTask: NpcPostUpdateTask by inject()

val pathFinderTask: PathFinderTask by inject()
val prePlayerUpdateTask: PlayerPreUpdateTask by inject()
val playerUpdateTask: PlayerUpdateTask by inject()
val playerPostUpdateTask: PlayerPostUpdateTask by inject()

val tasks: UpdateTaskList by inject()

tasks.register {
    -pathFinderTask
    -prePlayerUpdateTask
    -npcPreUpdateTask
    -playerUpdateTask
    -npcUpdateTask
    -playerPostUpdateTask
    -npcPostUpdateTask
}
