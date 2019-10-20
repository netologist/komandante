package com.hasanozgan.komandante

import arrow.effects.IO

interface CommandBus {
    fun publish(command: Command): IO<Command>
    fun <T : Command> subscribe(messageListener: MessageListener<in T>)

    fun addHandler(commandHandler: CommandHandler<out Command>)
    fun addHandler(commandHandler: CommandHandler<out Command>, onError: ErrorHandler)
}
