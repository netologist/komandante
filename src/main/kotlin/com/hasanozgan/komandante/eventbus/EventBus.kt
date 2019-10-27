package com.hasanozgan.komandante.eventbus

import arrow.effects.IO
import com.hasanozgan.komandante.*

interface EventBus {
    fun publish(message: Event): IO<Event>
    fun <T : Event> subscribe(messageListener: MessageListener<in T>)
    fun <T : Event> iterable(): Iterable<T>
    fun addHandler(eventHandler: EventHandler<out Event>)
    fun addHandler(eventHandler: EventHandler<out Event>, onError: ErrorHandler)
}