/*
Copyright (c) 2020 RS Mod

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/
package com.veosps.game.protocol.codec.login

import com.veosps.game.models.ClientMachine
import com.veosps.game.models.ClientSettings
import com.veosps.game.protocol.Device
import com.veosps.game.protocol.packet.server.InitialPlayerInfo
import com.veosps.game.util.security.IsaacRandom
import io.netty.channel.Channel

enum class LoginResponseType(val opcode: Int) {
    NORMAL(opcode = 2),
    RECONNECT(opcode = 15),
    PROFILE_TRANSFER(opcode = 21),
    RESTART_DECODER(opcode = 23),
    CUSTOM_ERROR(opcode = 29)
}

/**
 * Responsible for holding sensitive data during the log in process.
 *
 * @param password the password used to attempt log in. Can be null if
 * the request is a reconnection request.
 *
 * @param authCode the auth code used to attempt log in.
 *
 * @param xteas the XTEA key received from the client, if the request is
 * a reconnection, the key sent will be the one from the last successful
 * log in.
 */
data class LoginSecureBlock(
    val password: String?,
    val authCode: Int?,
    val xteas: IntArray,
    val reconnectXteas: IntArray?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginSecureBlock

        if (password != other.password) return false
        if (authCode != other.authCode) return false
        if (!xteas.contentEquals(other.xteas)) return false
        if (reconnectXteas != null) {
            if (other.reconnectXteas == null) return false
            if (!reconnectXteas.contentEquals(other.reconnectXteas)) return false
        } else if (other.reconnectXteas != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = password?.hashCode() ?: 0
        result = 31 * result + (authCode ?: 0)
        result = 31 * result + xteas.contentHashCode()
        result = 31 * result + (reconnectXteas?.contentHashCode() ?: 0)
        return result
    }
}

data class LoginRequest(
    val channel: Channel,
    val username: String,
    val password: String?,
    val device: Device,
    val email: Boolean,
    val reconnect: Boolean,
    val uuid: ByteArray,
    val authCode: Int?,
    val xteas: IntArray,
    val reconnectXteas: IntArray?,
    val settings: ClientSettings,
    val machine: ClientMachine
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginRequest

        if (channel != other.channel) return false
        if (username != other.username) return false
        if (password != other.password) return false
        if (device != other.device) return false
        if (email != other.email) return false
        if (reconnect != other.reconnect) return false
        if (!uuid.contentEquals(other.uuid)) return false
        if (authCode != other.authCode) return false
        if (!xteas.contentEquals(other.xteas)) return false
        if (reconnectXteas != null) {
            if (other.reconnectXteas == null) return false
            if (!reconnectXteas.contentEquals(other.reconnectXteas)) return false
        } else if (other.reconnectXteas != null) return false
        if (settings != other.settings) return false
        if (machine != other.machine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channel.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + (password?.hashCode() ?: 0)
        result = 31 * result + device.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + reconnect.hashCode()
        result = 31 * result + uuid.contentHashCode()
        result = 31 * result + (authCode ?: 0)
        result = 31 * result + xteas.contentHashCode()
        result = 31 * result + (reconnectXteas?.contentHashCode() ?: 0)
        result = 31 * result + settings.hashCode()
        result = 31 * result + machine.hashCode()
        return result
    }
}

sealed class LoginResponse(val type: LoginResponseType) {

    data class Normal(
        val playerIndex: Int,
        val privilege: Int,
        val moderator: Boolean,
        val rememberDevice: Boolean,
        val members: Boolean,
        val encodeIsaac: IsaacRandom
    ) : LoginResponse(LoginResponseType.NORMAL)

    data class Reconnect(
        val gpi: InitialPlayerInfo
    ) : LoginResponse(LoginResponseType.RECONNECT)

    data class Transfer(
        val secondsLeft: Int
    ) : LoginResponse(LoginResponseType.PROFILE_TRANSFER)

    class Restart : LoginResponse(LoginResponseType.RESTART_DECODER)

    data class Error(
        val errors: List<String>
    ) : LoginResponse(LoginResponseType.CUSTOM_ERROR)
}
