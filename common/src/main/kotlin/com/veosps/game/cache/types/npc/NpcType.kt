package com.veosps.game.cache.types.npc

import com.veosps.game.cache.types.CacheType

const val NPC_ARCHIVE = 2
const val NPC_GROUP = 9

data class NpcType(
    override val id: Int
) : CacheType