package com.hasanozgan.komandante

import arrow.core.extensions.`try`.monad.binding
import org.slf4j.LoggerFactory

class CommandHandler(val aggregateHandler: AggregateHandler, val aggregateFactory: AggregateFactory<*, *>) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun handle(command: Command) {
        binding {
            val currentAggregate = aggregateFactory.create(command.aggregateID)
            val (aggregate) = aggregateHandler.load(currentAggregate)

            aggregate.invokeHandle(command).leftMap {
                logger.error(it.message)
            }.toList().flatten().forEach {
                if (aggregate.id != it.aggregateID) {
                    logger.warn("event ${it} is ignored. event's aggregateId is different")
                } else {
                    aggregate.store(it);
                    aggregateHandler.save(aggregate)
                    logger.debug("event stored ${it}")
                }
            }
        }
    }
}
