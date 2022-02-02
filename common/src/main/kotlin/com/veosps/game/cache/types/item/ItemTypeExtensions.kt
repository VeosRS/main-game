package com.veosps.game.cache.types.item

private val EQUIPMENT_OPTION_PARAMS = intArrayOf(
    451,
    452,
    453,
    454,
    455,
    456,
    457,
    458
)

val ItemType.isNoted: Boolean
    get() = notedTemplate > 0

val ItemType.canBeNoted: Boolean
    get() = notedTemplate == 0 && notedId > 0

fun ItemType.equipmentOptions(): List<String> {
    val options = mutableListOf<String>()
    EQUIPMENT_OPTION_PARAMS.forEach { key ->
        val opt = parameters[key] ?: return@forEach
        options.add(opt.toString())
    }
    return options
}
