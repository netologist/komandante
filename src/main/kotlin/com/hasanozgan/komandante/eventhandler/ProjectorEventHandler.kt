package com.hasanozgan.komandante.eventhandler

import com.hasanozgan.komandante.*

class ProjectorEventHandler<T : Event>(val projector: Projector<T>, private val commandHandler: CommandHandler) : EventHandler<T> {
    override val handlerType: EventHandlerType
        get() = "projector"

    override fun <T : Event> handle(event: T) {
        projector.project(event).map {
            commandHandler.handle(it)
        }
    }
}