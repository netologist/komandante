package com.hasanozgan.komandante

import arrow.effects.IO

interface EventStore {
    fun load(aggregateID: AggregateID): IO<EventList>
    fun save(events: EventList, version: Int): IO<EventList>
}

