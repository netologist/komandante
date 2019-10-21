package com.hasanozgan.komandante.eventbus

import arrow.effects.IO
import com.hasanozgan.komandante.ErrorHandler
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventHandler
import com.hasanozgan.komandante.MessageListener

interface EventBus {
    fun publish(message: Event): IO<Event>
    fun <T : Event> subscribe(messageListener: MessageListener<in T>)

    fun addHandler(eventHandler: EventHandler<out Event>)
    fun addHandler(eventHandler: EventHandler<out Event>, onError: ErrorHandler)
}