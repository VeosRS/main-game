package com.veosps.game.cache.types.item

import com.github.michaelbull.logging.InlineLogger
import com.veosps.game.cache.GameCache
import com.veosps.game.cache.buffer.readParameters
import com.veosps.game.cache.buffer.readString
import com.veosps.game.cache.types.TypeLoader
import com.veosps.game.util.BeanScope
import io.netty.buffer.ByteBuf
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

const val ITEMS_ARCHIVE = 2
const val ITEMS_GROUP = 10

private val logger = InlineLogger()

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class ItemTypeLoader(
    override val cache: GameCache,
    private val types: ItemTypeList
) : TypeLoader {

    override fun load() {
        cache.groups(ITEMS_ARCHIVE, ITEMS_GROUP).forEach { (id, data) ->
            types.add(data.readType(id))
        }
        logger.info { "Loaded ${types.size} item definitions..." }
    }

    private fun ByteBuf.readType(id: Int): ItemType {
        val builder = ItemTypeBuilder().apply { this.id = id }

        while (isReadable) {
            val instruction = readUnsignedByte().toInt()
            if (instruction == 0) break

            builder.readBuffer(instruction, this)
        }

        return builder.build()
    }

    private fun ItemTypeBuilder.readBuffer(instruction: Int, buf: ByteBuf) {
        when (instruction) {
            1 -> inventoryModel = buf.readUnsignedShort()
            2 -> name = buf.readString()
            4 -> zoom2d = buf.readUnsignedShort()
            5 -> xan2d = buf.readUnsignedShort()
            6 -> yan2d = buf.readUnsignedShort()
            7 -> xOffset2d = buf.readShort().toInt()
            8 -> yOffset2d = buf.readShort().toInt()
            11 -> canStack = true
            12 -> cost = buf.readInt()
            16 -> members = true
            23 -> {
                maleModel0 = buf.readUnsignedShort()
                maleOffset = buf.readUnsignedByte().toInt()
            }
            24 -> maleModel1 = buf.readUnsignedShort()
            25 -> {
                femaleModel0 = buf.readUnsignedShort()
                femaleOffset = buf.readUnsignedByte().toInt()
            }
            26 -> femaleModel1 = buf.readUnsignedShort()
            in 30 until 35 -> {
                if (defaultGroundOptions) groundOptions = arrayOfNulls(5)
                val index = instruction - 30
                val option = buf.readString()
                groundOptions[index] = if (option == "Hidden") null else option
            }
            in 35 until 40 -> {
                if (defaultInterfaceOptions) interfaceOptions = arrayOfNulls(5)
                val index = instruction - 35
                val option = buf.readString()
                interfaceOptions[index] = option
            }
            40, 41 -> {
                val count = buf.readUnsignedByte().toInt()
                val src = IntArray(count)
                val dest = IntArray(count)
                repeat(count) {
                    src[it] = buf.readUnsignedShort()
                    dest[it] = buf.readUnsignedShort()
                }
                if (instruction == 40) {
                    colorFind = src
                    colorReplace = dest
                } else {
                    textureFind = src
                    textureReplace = dest
                }
            }
            42 -> shiftClickDropIndex = buf.readByte().toInt()
            65 -> canTrade = true
            78 -> maleModel2 = buf.readUnsignedShort()
            79 -> femaleModel2 = buf.readUnsignedShort()
            90 -> maleHeadModel0 = buf.readUnsignedShort()
            91 -> femaleHeadModel0 = buf.readUnsignedShort()
            92 -> maleHeadModel1 = buf.readUnsignedShort()
            93 -> femaleHeadModel1 = buf.readUnsignedShort()
            94 -> buf.readUnsignedShort()
            95 -> zan2d = buf.readUnsignedShort()
            97 -> notedId = buf.readUnsignedShort()
            98 -> notedTemplate = buf.readUnsignedShort()
            in 100 until 110 -> {
                if (countItem.isEmpty()) {
                    countItem = IntArray(10)
                    countCo = IntArray(10)
                }
                val index = instruction - 100
                countItem[index] = buf.readUnsignedShort()
                countCo[index] = buf.readUnsignedShort()
            }
            110 -> resizeX = buf.readUnsignedShort()
            111 -> resizeY = buf.readUnsignedShort()
            112 -> resizeZ = buf.readUnsignedShort()
            113 -> ambient = buf.readByte().toInt()
            114 -> contrast = buf.readByte().toInt()
            115 -> team = buf.readUnsignedByte().toInt()
            139 -> boughtId = buf.readUnsignedShort()
            140 -> boughtTemplateId = buf.readUnsignedShort()
            148 -> placeholderId = buf.readUnsignedShort()
            149 -> placeholderTemplateId = buf.readUnsignedShort()
            249 -> parameters = buf.readParameters()
            else -> error("Unrecognized item config code: $instruction")
        }
    }
}