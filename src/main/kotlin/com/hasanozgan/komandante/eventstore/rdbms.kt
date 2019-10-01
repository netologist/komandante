package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.rdbms.RdbmsEventStore

fun createRdbmsEventStore(): EventStore {
    return RdbmsEventStore()
}