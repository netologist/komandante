package com.hasanozgan.komandante.eventhandler

import arrow.core.Try
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventHandler
import com.hasanozgan.komandante.EventHandlerType

class ProjectorEventHandler : EventHandler<Event> {
    override val handlerType: EventHandlerType
        get() = "projector"

    override fun <T : Event> handle(event: T): Try<T> {
        return Try.just(event)
    }
}