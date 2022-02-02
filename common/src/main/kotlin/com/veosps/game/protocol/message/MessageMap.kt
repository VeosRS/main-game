package com.veosps.game.protocol.message

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.util.BeanScope
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

private typealias ServerPacketStructures = MutableMap<KClass<out ServerPacket>, ServerPacketStructure<*>>
private typealias ClientPacketStructures = MutableMap<Int, ClientPacketStructure<*>>

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class ServerPacketStructureMap(
    val structures: ServerPacketStructures = mutableMapOf()
) : Map<KClass<out ServerPacket>, ServerPacketStructure<*>> by structures {

    inline fun <reified T : ServerPacket> register(
        init: ServerPacketBuilder<T>.() -> Unit
    ) {
        val builder = ServerPacketBuilder<T>().apply(init)
        val structure = builder.build()
        if (structures.containsKey(T::class)) {
            error("Server packet type already has a structure (packet=${T::class.simpleName}).")
        }
        structures[T::class] = structure

        logger.debug {
            "Register server packet structure (packet=${T::class.simpleName}, " +
                "opcode=${structure.opcode}, length=${structure.length::class.simpleName})"
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified T : ServerPacket> get(packet: T) =
        structures[packet::class] as? ServerPacketStructure<T>

    companion object {
        val logger = InlineLogger()
    }
}

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class ClientPacketStructureMap(
    val injector: BeanFactory,
    val structures: ClientPacketStructures = mutableMapOf()
) : Map<Int, ClientPacketStructure<*>> by structures {

    inline fun <reified T : ClientPacket> register(
        init: ClientPacketBuilder<T>.() -> Unit
    ) {
        val builder = ClientPacketBuilder<T>().apply(init)
        val structureList = builder.build(injector)
        structureList.forEach { structure ->
            if (structures.containsKey(structure.opcode)) {
                error("Client packet opcode already has a structure (opcode=${structure.opcode}).")
            }
            structures[structure.opcode] = structure
        }

        logger.trace {
            val reference = structureList.first()
            val packet = T::class.simpleName
            val handler = reference.handler?.let { "handler=${it::class.simpleName}, " } ?: ""
            "Register client packet structure " +
                "(packet=$packet, ${handler}opcodes=${structureList.map { it.opcode }}, length=${reference.length})"
        }
    }

    companion object {
        val logger = InlineLogger()
    }
}
