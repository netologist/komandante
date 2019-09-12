package com.hasanozgan.komandante

import arrow.core.Try
import arrow.core.extensions.`try`.monad.binding

class CommandHandler(val aggregateHandler: AggregateHandler) {
    fun handle(command: Command): Try<Event> = binding {
        val (aggregate) = aggregateHandler.load(command.aggregateID)
        val (event) = aggregate.handle(command)
        val (appliedEvent) = aggregate.apply(event)
        aggregateHandler.save(aggregate)
        appliedEvent
    }
}
