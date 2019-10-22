package com.hasanozgan.komandante

import arrow.core.Failure
import arrow.core.Success
import arrow.core.Try
import arrow.core.extensions.`try`.monad.binding
import arrow.data.Invalid
import arrow.data.Valid

class CommandHandler(val aggregateHandler: AggregateHandler, val aggregateFactory: AggregateFactory<*, *>) {
    fun handle(command: Command): Try<Event> {
        return binding {
            val currentAggregate = aggregateFactory.create(command.aggregateID)
            val (aggregate) = aggregateHandler.load(currentAggregate)
            val commandResult = aggregate.handle(command)
            val (event) = when (commandResult) {
                is Valid -> Success(commandResult.a)
                is Invalid -> Failure(commandResult.e)
            }
            val (appliedEvent) = aggregate.store(event)
            val (stored) = aggregateHandler.save(aggregate).map { appliedEvent }

            stored
        }
    }
}
