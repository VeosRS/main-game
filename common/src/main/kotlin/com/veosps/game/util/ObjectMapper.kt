package com.veosps.game.util

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory())
    .findAndRegisterModules()
    .registerKotlinModule()
    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    .enable(SerializationFeature.INDENT_OUTPUT)

val jsonMapper: ObjectMapper = ObjectMapper()
    .findAndRegisterModules()
    .registerKotlinModule()
    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    .enable(SerializationFeature.INDENT_OUTPUT)