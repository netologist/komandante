package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.inmemory.InMemoryEventStore

fun newEventStoreWithInMemoryAdapter(): EventStore {
    return InMemoryEventStore()
}