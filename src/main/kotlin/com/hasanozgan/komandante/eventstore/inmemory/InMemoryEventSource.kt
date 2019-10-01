package com.hasanozgan.komandante.eventstore.inmemory

import arrow.effects.IO
import com.hasanozgan.komandante.*
import java.time.ZonedDateTime

internal class InMemoryEventStore : EventStore {
    private val eventDB = mutableMapOf<AggregateID, EventList>()

    override fun load(aggregateID: AggregateID): IO<EventList> {
        return IO.invoke { eventDB[aggregateID] ?: emptyList() }
    }

    override fun save(events: EventList, version: Int): IO<EventList> {
        if (events.isEmpty()) {
            return IO.raiseError(EventListEmptyError)
        }

        events.forEach{ event ->
            val i = events.indexOf(event)
            event.version = version + 1 + i
            event.timestamp = ZonedDateTime.now()

            eventDB.compute(event.aggregateID) { _, el -> (el ?: emptyList()).plus(event) }
        }

        return IO.invoke { events }
    }

}