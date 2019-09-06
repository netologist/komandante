package com.hasanozgan.komandante.eventbus

import io.reactivex.functions.Consumer

typealias EventHandler<T> = (T) -> Unit
typealias ErrorHandler = (error: Throwable) -> Unit

interface EventBus<T:Any> {
    fun publish(event: T)
    fun subscribe(eventHandler: EventHandler<T>)
    fun subscribe(eventHandler: EventHandler<T>, onError: ErrorHandler)
    fun <T> subscribeOf(eventHandler: EventHandler<in T>)
    fun <T> subscribeOf(eventHandler: EventHandler<in T>, onError: ErrorHandler)
}