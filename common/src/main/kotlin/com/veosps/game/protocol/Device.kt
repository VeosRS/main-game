package com.veosps.game.protocol

import com.veosps.game.models.ClientDevice

sealed class Device : ClientDevice {
    object Ios : Device()
    object Android : Device()
    object Desktop : Device()
    override fun toString(): String = javaClass.simpleName
}
