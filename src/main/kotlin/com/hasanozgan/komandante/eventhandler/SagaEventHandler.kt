package com.hasanozgan.komandante.eventhandler

import arrow.effects.extensions.io.applicativeError.handleError
import com.hasanozgan.komandante.*
import org.slf4j.LoggerFactory

class SagaEventHandler<T : Event>(val workflow: Workflow<T>, private val commandBus: CommandBus) : EventHandler<T> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override val handlerType: EventHandlerType
        get() = "saga"

    override fun <T : Event> handle(event: T) {
        workflow.run(event).forEach { command ->
            commandBus.publish(command).handleError {
                logger.error("commandbus failed: ${command}, error: ${it} in saga event handler")
            }
        }
    }
}