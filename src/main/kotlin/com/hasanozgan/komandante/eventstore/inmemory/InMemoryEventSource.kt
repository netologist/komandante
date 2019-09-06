package com.hasanozgan.komandante.eventstore.inmemory

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.DomainError
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventListEmptyError
import com.hasanozgan.komandante.EventStore
import java.time.ZonedDateTime

internal class InMemoryEventStore : EventStore {
    private val eventDB = mutableMapOf<AggregateID, List<Event>>()

    override fun load(aggregateID: AggregateID): List<Event> {
        return eventDB[aggregateID] ?: emptyList()
    }

    override fun save(events: List<Event>, version: Int): Option<DomainError> {
        if (events.isEmpty()) {
            return Some(EventListEmptyError)
        }

        val aggregateID = events[0].aggregateID

        for (event in events) {
            val i = events.indexOf(event)
            event.version = version + 1 + i
            event.timestamp = ZonedDateTime.now()

            eventDB.compute(event.aggregateID) { _, el -> (el ?: emptyList()).plus(event) }
        }
        return None
    }

}