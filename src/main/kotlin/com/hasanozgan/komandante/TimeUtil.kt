package com.hasanozgan.komandante

import org.joda.time.DateTime
import java.sql.Timestamp
import java.time.ZonedDateTime

fun ToDateTime(zonedDateTime: ZonedDateTime): DateTime {
    return DateTime(zonedDateTime.plusNanos(System.nanoTime()).toInstant().toEpochMilli())
}

fun ToTimestamp(zonedDateTime: ZonedDateTime): Timestamp {
    val timestamp = Timestamp(zonedDateTime.toEpochSecond())
    timestamp.setNanos((System.nanoTime() % 1000000000).toInt())
    return timestamp
}