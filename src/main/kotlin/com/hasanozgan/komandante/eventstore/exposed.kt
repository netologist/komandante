package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.exposed.ExposedEventStore

fun newEventStoreWithExposedAdapter(): EventStore {
    return ExposedEventStore()
}