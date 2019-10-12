package com.hasanozgan.komandante.eventstore.inmemory

import arrow.effects.IO
import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.EventList
import com.hasanozgan.komandante.EventListEmptyError
import com.hasanozgan.komandante.EventStore
import java.time.ZonedDateTime

internal class InMemoryEventStore : EventStore {
    private val eventDB = mutableMapOf<AggregateID, EventList>()

    override fun load(aggregateID: AggregateID): IO<EventList> {
        return IO.invoke { (eventDB[aggregateID])?.distinct() ?: emptyList() }
    }

    override fun save(events: EventList, version: Int): IO<EventList> {
        if (events.isEmpty()) {
            return IO.raiseError(EventListEmptyError)
        }

        events.forEach { event ->
            event.timestamp = ZonedDateTime.now()
            eventDB.compute(event.aggregateID) { _, el -> (el ?: emptyList()).plus(event) }
        }

        return IO.invoke { events.distinct() }
    }

}