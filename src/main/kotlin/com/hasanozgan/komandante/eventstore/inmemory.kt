package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.inmemory.InMemoryEventStore

fun createInMemoryEventStore(): EventStore {
    return InMemoryEventStore()
}