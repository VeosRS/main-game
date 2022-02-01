package com.veosps.game.plugin

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import kotlin.properties.ObservableProperty

open class Plugin(
    val injector: BeanFactory
) {

    inline fun <reified T : Any> inject(): ObservableProperty<T> = InjectedProperty(injector.getBean<T>())

    class InjectedProperty<T>(value: T) : ObservableProperty<T>(value)
}