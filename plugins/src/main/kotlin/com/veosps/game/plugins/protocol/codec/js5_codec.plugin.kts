package com.veosps.game.plugins.protocol.codec

import com.veosps.game.config.models.GameConfig
import com.veosps.game.net.handshake.HandshakeHandlerMap
import com.veosps.game.protocol.codec.HandshakeConstants
import com.veosps.game.protocol.codec.ResponseEncoder
import com.veosps.game.protocol.codec.js5.Js5Decoder
import com.veosps.game.protocol.codec.js5.Js5Dispatcher
import com.veosps.game.protocol.codec.js5.Js5Encoder
import com.veosps.game.protocol.codec.js5.Js5Handler

val handshakes: HandshakeHandlerMap by inject()
val dispatcher: Js5Dispatcher by inject()
val gameConfig: GameConfig by inject()

dispatcher.cacheResponses()

handshakes.register {
    opcode = HandshakeConstants.INIT_JS5REMOTE_CONNECTION
    decoder {
        name = HandshakeConstants.DECODER_PIPELINE
        provider = { Js5Decoder(gameConfig.majorRevision) }
    }
    encoder {
        name = HandshakeConstants.ENCODER_PIPELINE
        provider = { Js5Encoder }
    }
    adapter {
        name = HandshakeConstants.ADAPTER_PIPELINE
        provider = { Js5Handler(dispatcher) }
    }
    response {
        name = HandshakeConstants.RESPONSE_PIPELINE
        provider = { ResponseEncoder }
    }
}