package com.veosps.game.util

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.nio.file.Path
import com.fasterxml.jackson.module.kotlin.readValue
import com.veosps.game.models.ui.Component
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.springframework.beans.factory.config.ConfigurableBeanFactory

typealias BeanScope = ConfigurableBeanFactory
typealias UIComponent = Component

fun String.toPath(): Path = Path.of(this)
fun String.toFile(): File = File(this)
fun String.resolve(subPath: String): Path = Path.of(this, subPath)

inline fun <reified T> ObjectMapper.readValue(filePath: String): T = this.readValue(filePath.toFile())
inline fun <reified T> ObjectMapper.readValue(filePath: Path): T = this.readValue(filePath.toFile())
fun pathOf(vararg structure: String): Path = Path.of(structure.joinToString("/"))

fun ByteArray.toBuffer(): ByteBuf = Unpooled.wrappedBuffer(this)