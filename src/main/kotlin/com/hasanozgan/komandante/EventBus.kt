package com.hasanozgan.komandante

import arrow.effects.IO

interface EventBus {
    fun publish(message: Event): IO<Event>
    fun <T : Event> subscribe(messageListener: MessageListener<in T>)

    fun addHandler(eventHandler: EventHandler<out Event>)
    fun addHandler(eventHandler: EventHandler<out Event>, onError: ErrorHandler)
}