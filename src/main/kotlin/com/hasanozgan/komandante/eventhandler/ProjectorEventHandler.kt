package com.hasanozgan.komandante.eventhandler

import arrow.effects.extensions.io.applicativeError.handleError
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventHandler
import com.hasanozgan.komandante.EventHandlerType
import com.hasanozgan.komandante.Projector
import com.hasanozgan.komandante.commandbus.CommandBus
import org.slf4j.LoggerFactory

class ProjectorEventHandler<T : Event>(val projector: Projector<T>, private val commandBus: CommandBus) : EventHandler<T> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override val handlerType: EventHandlerType
        get() = "projector"

    override fun <T : Event> handle(event: T) {
        projector.invokeProject(event).map { command ->
            commandBus.publish(command).handleError {
                logger.error("commandbus failed: ${command}, error: ${it} in projector event handler")
            }
        }
    }
}