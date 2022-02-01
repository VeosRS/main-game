package com.veosps.game.protocol.packet.server

import com.veosps.game.protocol.message.ServerPacket

data class UpdateRunEnergy(val energy: Int) : ServerPacket

data class UpdateStat(val skill: Int, val currLevel: Int, val xp: Int) : ServerPacket
