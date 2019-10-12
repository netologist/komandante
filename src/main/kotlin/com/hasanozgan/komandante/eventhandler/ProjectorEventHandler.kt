package com.hasanozgan.komandante.eventhandler

import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventHandler
import com.hasanozgan.komandante.EventHandlerType
import com.hasanozgan.komandante.Projector

class ProjectorEventHandler<T : Event>(val projector: Projector<T>) : EventHandler<T> {
    override val handlerType: EventHandlerType
        get() = "projector"

    override fun <T : Event> handle(event: T) {
        projector.project(event)
    }
}