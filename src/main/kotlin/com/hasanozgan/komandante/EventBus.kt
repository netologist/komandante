package com.hasanozgan.komandante

import arrow.effects.IO

typealias EventListener<T> = (T) -> Unit
typealias ErrorHandler = (error: Throwable) -> Unit

interface EventBus<T : Event> {
    fun publish(event: T): IO<T>
    fun subscribe(eventListener: EventListener<T>)
    fun subscribe(eventListener: EventListener<T>, onError: ErrorHandler)
    fun <T> subscribeOf(eventListener: EventListener<in T>)
    fun <T> subscribeOf(eventListener: EventListener<in T>, onError: ErrorHandler)
    fun addHandler(eventHandler: EventHandler<out T>)
    fun addHandler(eventHandler: EventHandler<out T>, onError: ErrorHandler)
}