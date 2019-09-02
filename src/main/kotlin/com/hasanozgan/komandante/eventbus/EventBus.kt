package com.hasanozgan.komandante.eventbus

typealias EventHandler<T> = (T) -> Unit

interface EventBus<T> {
    fun publish(event: T)
    fun subscribe(eventHandler: EventHandler<T>)
    fun <T> subscribeOf(eventHandler: EventHandler<in T>)
}