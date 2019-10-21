package com.hasanozgan.komandante.commandbus

import arrow.effects.IO
import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.CommandHandler
import com.hasanozgan.komandante.ErrorHandler
import com.hasanozgan.komandante.MessageListener

interface CommandBus {
    fun publish(command: Command): IO<Command>
    fun <T : Command> subscribe(messageListener: MessageListener<in T>)

    fun addHandler(commandHandler: CommandHandler<out Command>)
    fun addHandler(commandHandler: CommandHandler<out Command>, onError: ErrorHandler)
}
