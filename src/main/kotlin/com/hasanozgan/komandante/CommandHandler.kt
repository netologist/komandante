package com.hasanozgan.komandante

import arrow.core.extensions.`try`.monad.binding
import arrow.core.toOption
import org.slf4j.LoggerFactory

class CommandHandler(val aggregateHandler: AggregateHandler, val aggregateFactory: AggregateFactory<*, *>) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun handle(command: Command) {
        binding {
            val currentAggregate = aggregateFactory.create(command.aggregateID)
            val (aggregate) = aggregateHandler.load(currentAggregate)

            aggregate.invokeHandle(command).leftMap {
                logger.error(it.message)
            }.toList().flatten()
                    .map { aggregate.store(it) }
                    .takeIf { it.isNotEmpty() }
                    .toOption().map { aggregateHandler.save(aggregate) }
        }
    }
}
