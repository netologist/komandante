package com.hasanozgan.komandante.eventbus

import arrow.effects.IO

typealias EventHandler<T> = (T) -> Unit
typealias ErrorHandler = (error: Throwable) -> Unit

interface EventBus<T : Any> {
    fun publish(event: T): IO<T>
    fun subscribe(eventHandler: EventHandler<T>)
    fun subscribe(eventHandler: EventHandler<T>, onError: ErrorHandler)
    fun <T> subscribeOf(eventHandler: EventHandler<in T>)
    fun <T> subscribeOf(eventHandler: EventHandler<in T>, onError: ErrorHandler)
}