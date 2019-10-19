package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event

fun forceUpdateAggregateID(event: Event, aggregateID: AggregateID) {
    event.javaClass.superclass.declaredFields.filter { it.name.equals("aggregateID") }.forEach {
        it.setAccessible(true);
        it.set(event, aggregateID)
    }
}