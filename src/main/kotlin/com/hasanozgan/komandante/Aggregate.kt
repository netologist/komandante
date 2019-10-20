package com.hasanozgan.komandante

import arrow.core.Try
import arrow.data.Validated
import java.util.*

typealias AggregateID = UUID

fun newAggregateID(): AggregateID {
    return AggregateID.randomUUID()
}

interface Aggregate {
    var id: AggregateID
    var events: MutableList<Event>
    var version: Int

    fun handle(command: Command): Validated<DomainError, Event>

    fun store(event: Event): Try<Event> {
        return apply(event).map {
            incrementVersion()
            it.version = version
            events.add(it); it
        }
    }

    fun apply(event: Event): Try<Event>

    fun incrementVersion() {
        version++
    }
}