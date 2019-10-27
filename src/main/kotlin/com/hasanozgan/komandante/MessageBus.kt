package com.hasanozgan.komandante

import arrow.effects.IO

typealias MessageListener<T> = (T) -> Unit
typealias MessageListener2<T> = suspend (T) -> Unit
typealias ErrorHandler = (error: Throwable) -> Unit

interface MessageBus {
    fun <T : Message> publish(message: T): IO<T>
    fun <T : Message> iterable(): Iterable<T>
    fun subscribe(messageListener: MessageListener<Message>)
    fun subscribe(messageListener: MessageListener<Message>, onError: ErrorHandler)
    fun subscribe(filterType: Class<*>, messageListener: MessageListener<Message>)
    fun subscribe(filterType: Class<*>, messageListener: MessageListener<Message>, onError: ErrorHandler)
    fun <T : Message> subscribeOf(messageListener: MessageListener<in T>)
    fun <T : Message> subscribeOf(messageListener: MessageListener<in T>, onError: ErrorHandler)
}