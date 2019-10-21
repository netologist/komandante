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

    override fun addHandler(commandHandler: CommandHandler<out Command>) {
        this.addHandler(commandHandler, DefaultErrorHandler)
    }

    override fun addHandler(commandHandler: CommandHandler<out Command>, onError: ErrorHandler) {
        //TODO: Type detection required
        messageBus.subscribe(Command::class.java, {
            when (val result = commandHandler.handle(it as Command)) {
                is Failure -> {
                    logger.error(result.exception.message)
                    // TODO: Decide about exception error or warning?
//                    throw result.exception
                }
            }
        }, { onError(it) })
    }
}
