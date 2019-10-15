package com.hasanozgan.komandante.eventhandler

import com.hasanozgan.komandante.*

class SagaEventHandler<T : Event>(val workflow: Workflow<T>, private val commandHandler: CommandHandler) : EventHandler<T> {
    override val handlerType: EventHandlerType
        get() = "saga"

    override fun <T : Event> handle(event: T) {
        workflow.run(event).forEach {
            if (commandHandler.handle(it).isFailure()) {
                // TOOD: add logger
                println("workflow failed: ${it}")
            }
        }
    }
}