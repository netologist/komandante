package com.hasanozgan.komandante

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.hasanozgan.komandante.eventbus.EventBus

class AggregateHandler(private val store: EventStore, private val bus: EventBus<Event>, private val aggregateFactory: AggregateFactory) {
    fun load(aggregateID: AggregateID): Result<Aggregate, DomainError> {
        val eventList = store.load(aggregateID)

        val aggregate = aggregateFactory.create(aggregateID)
        for (event in eventList) {
            val res = aggregate.apply(event)
            when (res) {
                is Reject -> {
                    println("Event: '${event}' rejected. (Reason: ${res.reason})")
                    return res
                }
                is Accept -> println("Event: '${event}' accepted.")
            }
        }

        return Accept(aggregate)
    }


    fun save(aggregate: Aggregate): Option<DomainError> {

        val events = aggregate.events.toMutableList()

        val maybeSaveError = store.save(events, aggregate.version)
        if (maybeSaveError.isDefined()) {
            return maybeSaveError
        }

        // Why???
        aggregate.clearEvents()


        // apply events
        for (event in events) {
            val res = aggregate.apply(event)
            when (res) {
                is Reject -> {
                    println("Event: '${event}' rejected. (Reason: ${res.reason})")
                    return Some(res.reason)
                }
                is Accept -> println("Event: '${event}' accepted.")
            }

            aggregate.incrementVersion()
        }

        for (event in events) {
            bus.publish(event)
        }

        return None
    }
}