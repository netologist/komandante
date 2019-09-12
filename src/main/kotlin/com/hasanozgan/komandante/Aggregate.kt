package com.hasanozgan.komandante

import arrow.core.Try
import java.util.*

typealias AggregateID = UUID

fun newAggregateID(): AggregateID {
    return AggregateID.randomUUID()
}

typealias AggregateType = String

interface Aggregate {
    var id: AggregateID
    var events: MutableList<Event>
    var version: Int

    fun handle(command: Command): Try<Event>

    fun apply(event: Event): Try<Event>

    fun storeEvent(event:Event) {
        events.add(event)
    }

    fun clearEvents() {
        events.clear()
    }

    fun incrementVersion() {
        version++
    }
}