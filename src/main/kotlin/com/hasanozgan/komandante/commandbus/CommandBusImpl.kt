package com.hasanozgan.komandante.commandbus

import arrow.effects.IO
import com.hasanozgan.komandante.*
import com.hasanozgan.komandante.messagebus.DefaultErrorHandler

fun localCommandBus(messageBus: MessageBus): CommandBus = LocalCommandBus(messageBus)

class LocalCommandBus(val messageBus: MessageBus) : CommandBus {
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
        messageBus.subscribe(Command::class.java, {
            commandHandler.handle(it as Command)
        }, { onError(it) })
    }
}
