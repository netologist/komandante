package com.hasanozgan.komandante

import arrow.effects.IO

typealias EventListener<T> = (T) -> Unit
typealias EventHandlerError = (error: Throwable) -> Unit

interface EventBus<T : Event> {
    fun publish(event: T): IO<T>
    fun <K> publish(event: K): IO<K>
    fun subscribe(eventListener: EventListener<T>)
    fun subscribe(eventListener: EventListener<T>, onError: EventHandlerError)
    fun <T> subscribeOf(eventListener: EventListener<in T>)
    fun <T> subscribeOf(eventListener: EventListener<in T>, onError: EventHandlerError)
    fun addHandler(eventHandler: EventHandler<out Event>)
    fun addHandler(eventHandler: EventHandler<out Event>, onError: EventHandlerError)
}