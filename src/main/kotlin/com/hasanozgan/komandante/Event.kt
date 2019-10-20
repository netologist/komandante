package com.hasanozgan.komandante

import java.time.ZonedDateTime

abstract class Event() : Message {
    var timestamp = ZonedDateTime.now()
    open var version: Int = 0
    abstract val aggregateID: AggregateID
}

typealias EventList = List<Event>