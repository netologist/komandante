package com.hasanozgan.komandante

import arrow.effects.IO

typealias EventListener<T> = (T) -> Unit
typealias EventHandlerError = (error: Throwable) -> Unit

interface EventBus {
    fun <T : Event> publish(event: T): IO<T>
    fun subscribe(eventListener: EventListener<Event>)
    fun subscribe(eventListener: EventListener<Event>, onError: EventHandlerError)
    fun <T : Event> subscribeOf(eventListener: EventListener<in T>)
    fun <T : Event> subscribeOf(eventListener: EventListener<in T>, onError: EventHandlerError)
    fun addHandler(eventHandler: EventHandler<out Event>)
    fun addHandler(eventHandler: EventHandler<out Event>, onError: EventHandlerError)
}