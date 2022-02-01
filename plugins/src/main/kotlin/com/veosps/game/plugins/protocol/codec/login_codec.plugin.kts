package com.veosps.game.plugins.protocol.codec

import com.veosps.game.cache.GameCache
import com.veosps.game.config.models.GameConfig
import com.veosps.game.config.models.RsaConfig
import com.veosps.game.net.handshake.HandshakeHandlerMap
import com.veosps.game.protocol.codec.HandshakeConstants
import com.veosps.game.protocol.codec.ResponseEncoder
import com.veosps.game.protocol.codec.account.AccountDispatcher
import com.veosps.game.protocol.codec.js5.Js5Decoder
import com.veosps.game.protocol.codec.js5.Js5Dispatcher
import com.veosps.game.protocol.codec.js5.Js5Encoder
import com.veosps.game.protocol.codec.js5.Js5Handler
import com.veosps.game.protocol.codec.login.LoginDecoder
import com.veosps.game.protocol.codec.login.LoginEncoder
import com.veosps.game.protocol.codec.login.LoginHandler
import com.veosps.game.protocol.packet.login.LoginPacketMap

val dispatcher: AccountDispatcher by inject()
val handshakes: HandshakeHandlerMap by inject()
val loginPackets: LoginPacketMap by inject()
val gameConfig: GameConfig by inject()
val rsaConfig: RsaConfig by inject()
val cache: GameCache by inject()

dispatcher.start()

handshakes.register {
    opcode = HandshakeConstants.INIT_GAME_CONNECTION
    decoder {
        name = HandshakeConstants.DECODER_PIPELINE
        provider = {
            LoginDecoder(
                majorRevision = gameConfig.majorRevision,
                minorRevision = gameConfig.minorRevision,
                rsaConfig = rsaConfig,
                cacheCrcs = cache.archiveCrcs,
                loginPackets = loginPackets
            )
        }
    }
    encoder {
        name = HandshakeConstants.ENCODER_PIPELINE
        provider = { LoginEncoder }
    }
    adapter {
        name = HandshakeConstants.ADAPTER_PIPELINE
        provider = { LoginHandler(dispatcher) }
    }
    response {
        name = HandshakeConstants.RESPONSE_PIPELINE
        provider = { ResponseEncoder }
    }
}