package com.hasanozgan.komandante

import arrow.effects.IO

typealias CommandHandlerError = (error: Throwable) -> Unit

interface CommandBus {
    fun publish(command: Command): IO<Command>
    fun addHandler(aggregateHandler: AggregateHandler)
    fun addHandler(aggregateHandler: AggregateHandler, onError: CommandHandlerError)
    fun <T : Command> addHandlerOf(clazz: Class<T>, aggregateHandler: AggregateHandler)
    fun <T : Command> addHandlerOf(clazz: Class<T>, aggregateHandler: AggregateHandler, onError: CommandHandlerError)
}