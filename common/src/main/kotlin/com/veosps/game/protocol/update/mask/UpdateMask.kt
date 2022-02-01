package com.veosps.game.protocol.update.mask

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.util.BeanScope
import io.netty.buffer.ByteBuf
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

private val logger = InlineLogger()

interface UpdateMask

class UpdateMaskSet(
    private val masks: MutableSet<UpdateMask> = mutableSetOf()
) : Set<UpdateMask> by masks {

    fun <T : UpdateMask> add(mask: T) {
        masks.removeIf { it::class == mask::class }
        masks.add(mask)
    }

    fun <T : UpdateMask> remove(type: KClass<T>) {
        masks.removeIf { it::class == type }
    }

    fun clear() {
        masks.clear()
    }

    fun <T : UpdateMask> contains(type: KClass<T>): Boolean {
        return masks.any { it::class == type }
    }

    override fun contains(element: UpdateMask): Boolean {
        throw UnsupportedOperationException("Use the contains(KClass<UpdateMask>) function instead.")
    }

    override fun containsAll(elements: Collection<UpdateMask>): Boolean {
        throw UnsupportedOperationException("Unsupported operation.")
    }
}

data class UpdateMaskPacket<T : UpdateMask>(
    val mask: Int,
    val write: T.(ByteBuf) -> Unit
)

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class UpdateMaskPacketMap(
    val handlers: MutableMap<KClass<out UpdateMask>, UpdateMaskPacket<*>> = mutableMapOf(),
    val order: MutableList<KClass<out UpdateMask>> = mutableListOf()
) : Map<KClass<out UpdateMask>, UpdateMaskPacket<*>> by handlers {

    inline fun <reified T : UpdateMask> register(
        init: UpdateMaskPacketBuilder<T>.() -> Unit
    ) {
        val builder = UpdateMaskPacketBuilder<T>().apply(init)
        val handler = builder.build()
        if (handlers.containsKey(T::class)) {
            error("Update mask handler already exists (type=${T::class.simpleName})")
        } else if (handlers.values.any { it.mask == handler.mask }) {
            error("Update mask handler already exists (mask=${handler.mask.formatMask()})")
        }
        logger.debug { "Register update mask handler (type=${T::class.simpleName}, mask=${handler.mask.formatMask()})" }
        handlers[T::class] = handler
    }

    fun order(init: UpdateMaskOrderBuilder.() -> Unit) {
        UpdateMaskOrderBuilder(order).apply(init)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : UpdateMask> get(mask: T): UpdateMaskPacket<T>? {
        return handlers[mask::class] as? UpdateMaskPacket<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : UpdateMask> getValue(mask: T): UpdateMaskPacket<T> {
        return handlers.getValue(mask::class) as UpdateMaskPacket<T>
    }

    companion object {

        val logger = InlineLogger()

        fun Int.formatMask(): String {
            return "0x${toString(16)}"
        }
    }
}

@DslMarker
private annotation class BuilderDslMarker

@BuilderDslMarker
class UpdateMaskPacketBuilder<T : UpdateMask>(
    var mask: Int = 0,
    private var writer: (T.(ByteBuf) -> Unit)? = null
) {

    fun write(writer: T.(ByteBuf) -> Unit) {
        this.writer = writer
    }

    fun build(): UpdateMaskPacket<T> {
        val mask = if (mask == 0) error("Handler mask has not been set.") else mask
        val writer = writer ?: error("Handler writer has not been set.")
        return UpdateMaskPacket(mask, writer)
    }
}

@BuilderDslMarker
class UpdateMaskOrderBuilder(private val order: MutableList<KClass<out UpdateMask>>) {

    operator fun KClass<out UpdateMask>.unaryMinus() {
        logger.debug { "Append update mask order (type=${this.simpleName})" }
        order.add(this)
    }
}
