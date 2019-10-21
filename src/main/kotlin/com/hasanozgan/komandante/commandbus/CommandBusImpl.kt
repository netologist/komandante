package com.hasanozgan.komandante.commandbus

import arrow.core.Failure
import arrow.effects.IO
import com.hasanozgan.komandante.*
import com.hasanozgan.komandante.messagebus.DefaultErrorHandler
import org.slf4j.LoggerFactory

fun newCommandBus(messageBus: MessageBus): CommandBus = CommandBusImpl(messageBus)

class CommandBusImpl(val messageBus: MessageBus) : CommandBus {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun publish(command: Command): IO<Command> {
        return messageBus.publish(command)
    }

    override fun <T : Command> subscribe(messageListener: MessageListener<in T>) {
        messageBus.subscribeOf<T>(messageListener)
    }

    override fun <T : Command> addHandler(filterType: Class<T>, commandHandler: CommandHandler) {
        this.addHandler<T>(filterType, commandHandler, DefaultErrorHandler)
    }

    override fun <T : Command> addHandler(filterType: Class<T>, commandHandler: CommandHandler, onError: ErrorHandler) {
        messageBus.subscribe(filterType, {
            when (val result = commandHandler.handle(it as Command)) {
                is Failure -> {
                    logger.warn(result.exception.message)
                }
            }
        }, { onError(it) })
    }
}
