package com.veosps.game.action

interface Action

typealias ActionExecutor<T> = (T).() -> Unit
