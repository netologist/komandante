package com.hasanozgan.komandante.eventstore.rdbms

import arrow.effects.IO
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventList
import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.rdbms.dao.Events
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.text.SimpleDateFormat
import java.text.DateFormat




class RdbmsEventStore : EventStore {
    override fun load(aggregateID: AggregateID): IO<EventList> {
        return IO.just(listOf())

//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(events: List<Event>, version: Int): IO<EventList> {
        var objectMapper = ObjectMapper()
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
//        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm a z")

        objectMapper.setDateFormat(df)
//        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true)
        for (e in events) {
            var writeValueAsString = objectMapper.writeValueAsString(e)
            println(writeValueAsString)
            println(e.javaClass.canonicalName)
            println(e.javaClass.declaredFields.iterator().forEach { println(it.name) })

        }
        return IO.just(listOf())
    }
}