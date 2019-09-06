package com.hasanozgan.komandante

import arrow.core.Option
import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.DomainError
import com.hasanozgan.komandante.Event

interface EventStore {
    fun load(aggregateID: AggregateID): List<Event>
    fun save(events: List<Event>, version: Int): Option<DomainError>
}

