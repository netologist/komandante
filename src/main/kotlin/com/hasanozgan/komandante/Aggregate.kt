package com.hasanozgan.komandante

import arrow.core.None
import arrow.core.Option
import java.util.*

typealias AggregateID = UUID

fun newAggregateID(): AggregateID {
    return AggregateID.randomUUID()
}

typealias AggregateType = String

interface Aggregate : CommandHandler {
    var id: AggregateID
    var events: MutableList<Event>
    var version: Int

    fun apply(event: Event): Result<Event, DomainError>

    fun storeEvent(event:Event): Option<DomainError> {
        events.add(event)
        return None
    }

    fun clearEvents() {
        events.clear()
    }

    fun incrementVersion() {
        version++
    }
}