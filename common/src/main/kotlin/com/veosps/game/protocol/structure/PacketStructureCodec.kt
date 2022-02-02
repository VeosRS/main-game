package com.veosps.game.protocol.structure

import com.veosps.game.protocol.Device
import com.veosps.game.protocol.message.ClientPacketStructureMap
import com.veosps.game.protocol.message.ServerPacketStructureMap
import com.veosps.game.protocol.update.mask.UpdateMaskPacketMap
import com.veosps.game.util.BeanScope
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class PacketStructureCodec(
    val server: ServerPacketStructureMap,
    val client: ClientPacketStructureMap,
    val playerUpdate: UpdateMaskPacketMap,
    val npcUpdate: UpdateMaskPacketMap
)

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class DevicePacketStructureMap(
    private val beanFactory: BeanFactory,
    private val desktop: PacketStructureCodec = codec(beanFactory),
    private val ios: PacketStructureCodec = codec(beanFactory),
    private val android: PacketStructureCodec = codec(beanFactory)
) {

    fun client(device: Device) = getCodec(device).client

    fun server(device: Device) = getCodec(device).server

    fun playerUpdate(device: Device) = getCodec(device).playerUpdate

    fun npcUpdate(device: Device) = getCodec(device).npcUpdate

    fun getCodec(device: Device): PacketStructureCodec = when (device) {
        Device.Desktop -> desktop
        Device.Ios -> ios
        Device.Android -> android
    }

    companion object {

        private fun codec(injector: BeanFactory) = PacketStructureCodec(
            ServerPacketStructureMap(),
            ClientPacketStructureMap(injector),
            UpdateMaskPacketMap(),
            UpdateMaskPacketMap()
        )
    }
}
