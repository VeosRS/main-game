package com.veosps.game.protocol.codec.account

import com.veosps.game.models.Client
import com.veosps.game.protocol.Device
import com.veosps.game.util.security.IsaacRandom
import io.netty.channel.Channel

data class Account(
    val channel: Channel,
    val client: Client,
    val device: Device,
    val decodeIsaac: IsaacRandom,
    val encodeIsaac: IsaacRandom,
    val newAccount: Boolean
)
