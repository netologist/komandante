package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event
import org.joda.time.DateTime
import java.sql.Timestamp
import java.time.ZonedDateTime

// time utils
fun toDateTime(zonedDateTime: ZonedDateTime): DateTime {
    return DateTime(zonedDateTime.plusNanos(System.nanoTime()).toInstant().toEpochMilli())
}

fun toTimestamp(zonedDateTime: ZonedDateTime): Timestamp {
    val timestamp = Timestamp(zonedDateTime.toEpochSecond())
    timestamp.setNanos((System.nanoTime() % 1000000000).toInt())
    return timestamp
}

// event utils
fun forceUpdateAggregateID(event: Event, aggregateID: AggregateID) {
    event.javaClass.superclass.declaredFields.filter { it.name.equals("aggregateID") }.forEach {
        it.setAccessible(true);
        it.set(event, aggregateID)
    }
}