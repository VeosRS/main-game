@file:Suppress("MemberVisibilityCanBePrivate")

package com.veosps.game.cache.types.item

import com.veosps.game.cache.types.TypeBuilder

private val DEFAULT_GROUND_OPTIONS = arrayOf(null, null, "Take", null, null)
private val DEFAULT_INTERFACE_OPTIONS = arrayOf(null, null, null, null, "Drop")

class ItemTypeBuilder(
    var id: Int = -1,
    var name: String = "null",

    var cost: Int = 1,
    var canTrade: Boolean = false,
    var canStack: Boolean = false,
    var members: Boolean = false,
    var category: Int = -1,
    var notedId: Int = -1,
    var notedTemplate: Int = -1,
    var team: Int = 0,
    var shiftClickDropIndex: Int = -2,
    var boughtId: Int = 0,
    var boughtTemplateId: Int = 0,
    var parameters: Map<Int, Any> = emptyMap(),

    var groundOptions: Array<String?> = DEFAULT_GROUND_OPTIONS,
    var interfaceOptions: Array<String?> = DEFAULT_INTERFACE_OPTIONS,

    var placeholderId: Int = 0,
    var placeholderTemplateId: Int = 0,

    var countCo: IntArray = IntArray(0),
    var countItem: IntArray = IntArray(0),

    var inventoryModel: Int = 0,
    var maleModel0: Int = 0,
    var maleModel1: Int = 0,
    var maleModel2: Int = 0,
    var maleOffset: Int = 0,
    var maleHeadModel0: Int = 0,
    var maleHeadModel1: Int = 0,
    var femaleModel0: Int = 0,
    var femaleModel1: Int = 0,
    var femaleModel2: Int = 0,
    var femaleOffset: Int = 0,
    var femaleHeadModel0: Int = 0,
    var femaleHeadModel1: Int = 0,

    var resizeX: Int = 128,
    var resizeY: Int = 128,
    var resizeZ: Int = 128,
    var xan2d: Int = 0,
    var yan2d: Int = 0,
    var zan2d: Int = 0,
    var zoom2d: Int = 2000,
    var xOffset2d: Int = 0,
    var yOffset2d: Int = 0,

    var colorFind: IntArray = IntArray(0),
    var colorReplace: IntArray = IntArray(0),
    var textureFind: IntArray = IntArray(0),
    var textureReplace: IntArray = IntArray(0),
    var ambient: Int = 0,
    var contrast: Int = 0,
) : TypeBuilder<ItemType> {

    val defaultGroundOptions: Boolean
        get() = groundOptions === DEFAULT_GROUND_OPTIONS

    val defaultInterfaceOptions: Boolean
        get() = interfaceOptions === DEFAULT_INTERFACE_OPTIONS

    override fun build(): ItemType {
        check(id != -1) { "Item type id has not been set." }
        return ItemType(
            id = id,
            name = name,
            cost = cost,
            canTrade = canTrade,
            canStack = canStack,
            members = members,
            category = category,
            notedId = notedId,
            notedTemplate = notedTemplate,
            team = team,
            shiftClickDropIndex = shiftClickDropIndex,
            boughtId = boughtId,
            boughtTemplateId = boughtTemplateId,
            parameters = parameters,
            groundOptions = groundOptions.toList(),
            interfaceOptions = interfaceOptions.toList(),
            placeholderId = placeholderId,
            placeholderTemplateId = placeholderTemplateId,
            countCo = countCo.toList(),
            countItem = countItem.toList(),
            inventoryModel = inventoryModel,
            maleModel0 = maleModel0,
            maleModel1 = maleModel1,
            maleModel2 = maleModel2,
            maleOffset = maleOffset,
            maleHeadModel0 = maleHeadModel0,
            maleHeadModel1 = maleHeadModel1,
            femaleModel0 = femaleModel0,
            femaleModel1 = femaleModel1,
            femaleModel2 = femaleModel2,
            femaleOffset = femaleOffset,
            femaleHeadModel0 = femaleHeadModel0,
            femaleHeadModel1 = femaleHeadModel1,
            resizeX = resizeX,
            resizeY = resizeY,
            resizeZ = resizeZ,
            xan2d = xan2d,
            yan2d = yan2d,
            zan2d = zan2d,
            zoom2d = zoom2d,
            xOffset2d = xOffset2d,
            yOffset2d = yOffset2d,
            colorFind = colorFind.toList(),
            colorReplace = colorReplace.toList(),
            textureFind = textureFind.toList(),
            textureReplace = textureReplace.toList(),
            ambient = ambient,
            contrast = contrast
        )
    }
}