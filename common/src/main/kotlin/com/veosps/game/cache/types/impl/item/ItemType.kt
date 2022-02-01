package com.veosps.game.cache.types.impl.item

import com.veosps.game.cache.types.CacheType

data class ItemType(
    override val id: Int,
    val name: String,

    val cost: Int,
    val canTrade: Boolean,
    val canStack: Boolean,
    val members: Boolean,
    val category: Int,
    val notedId: Int,
    val notedTemplate: Int,
    val team: Int,
    val shiftClickDropIndex: Int,
    val boughtId: Int,
    val boughtTemplateId: Int,
    val parameters: Map<Int, Any>,

    val groundOptions: List<String?>,
    val interfaceOptions: List<String?>,

    val placeholderId: Int,
    val placeholderTemplateId: Int,

    val countCo: List<Int>,
    val countItem: List<Int>,

    val inventoryModel: Int,
    val maleModel0: Int,
    val maleModel1: Int,
    val maleModel2: Int,
    val maleOffset: Int,
    val maleHeadModel0: Int,
    val maleHeadModel1: Int,
    val femaleModel0: Int,
    val femaleModel1: Int,
    val femaleModel2: Int,
    val femaleOffset: Int,
    val femaleHeadModel0: Int,
    val femaleHeadModel1: Int,

    val resizeX: Int,
    val resizeY: Int,
    val resizeZ: Int,
    val xan2d: Int,
    val yan2d: Int,
    val zan2d: Int,
    val zoom2d: Int,
    val xOffset2d: Int,
    val yOffset2d: Int,

    val colorFind: List<Int>,
    val colorReplace: List<Int>,
    val textureFind: List<Int>,
    val textureReplace: List<Int>,
    val ambient: Int,
    val contrast: Int,
) : CacheType