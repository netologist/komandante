package com.hasanozgan.komandante.eventhandler

import arrow.core.Try
import com.hasanozgan.komandante.*
import org.slf4j.LoggerFactory

class SagaEventHandler<T : Event>(val workflow: Workflow<T>, private val commandHandler: CommandHandler) : EventHandler<T> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override val handlerType: EventHandlerType
        get() = "saga"

    override fun <T : Event> handle(event: T) {
        workflow.run(event).forEach {
            when (val maybeCommand = commandHandler.handle(it)) {
                is Try.Failure -> logger.error("workflow failed: ${it}, error: ${maybeCommand.exception.message}")
            }
        }
    }
}