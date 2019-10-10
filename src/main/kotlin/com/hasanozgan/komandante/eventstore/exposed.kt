package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.exposed.ExposedEventStore

fun createExposedEventStore(): EventStore {
    return ExposedEventStore()
}