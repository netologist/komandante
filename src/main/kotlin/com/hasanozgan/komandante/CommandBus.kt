package com.hasanozgan.komandante

import arrow.effects.IO

typealias CommandListener<T> = (T) -> Unit
typealias CommandHandlerError = (error: Throwable) -> Unit

interface CommandBus {
    fun publish(command: Command): IO<Command>
    fun subscribe(commandListener: CommandListener<Command>)
    fun subscribe(commandListener: CommandListener<Command>, onError: CommandHandlerError)
    fun <T : Command> subscribeOf(commandListener: CommandListener<in T>)
    fun <T : Command> subscribeOf(commandListener: CommandListener<in T>, onError: CommandHandlerError)
    fun addHandler(aggregateHandler: AggregateHandler)
    fun addHandler(aggregateHandler: AggregateHandler, onError: CommandHandlerError)
    fun <T : Command> addHandler(aggregateHandler: AggregateHandler, baseCommandClass: Class<T>)
    fun <T : Command> addHandler(aggregateHandler: AggregateHandler, baseCommandClass: Class<T>, onError: CommandHandlerError)
}
