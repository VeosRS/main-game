package com.veosps.game.config.models

data class ServiceConfig(
    val xteaLoader: String = "XteaFileLoader",
    val xteaRepository: String = "XteaInMemoryRepository"
)