package com.veosps.game

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    SpringApplicationBuilder(Application::class.java)
        .web(WebApplicationType.NONE)
        .run(*args)
}