package com.hasanozgan.komandante.eventstore.exposed


import arrow.data.extensions.list.foldable.exists
import arrow.effects.IO
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventList
import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.exposed.dao.Events
import com.hasanozgan.komandante.eventstore.exposed.dao.Events.canonicalName
import com.hasanozgan.komandante.eventstore.exposed.dao.Events.timestamp
import com.hasanozgan.komandante.eventstore.exposed.dao.Events.values
import com.hasanozgan.komandante.eventstore.exposed.dao.Events.version
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import kotlin.reflect.full.declaredMembers

class CustomExclusionStrategy : ExclusionStrategy {

    override fun shouldSkipField(f: FieldAttributes): Boolean {
        return Event::class.declaredMembers.map { it.name }.exists { it == f.name }
    }

    override fun shouldSkipClass(clazz: Class<*>): Boolean {
        return false
    }
}

class ExposedEventStore : EventStore {
    override fun load(aggregateID: AggregateID): IO<EventList> {
        val result = transaction {
            val result = mutableListOf<Event>()
            val gson = GsonBuilder().setExclusionStrategies(CustomExclusionStrategy()).create()

            Events.select { Events.aggregateID.eq(aggregateID) }.sortedBy { timestamp }.map {
                val eventClazz = Class.forName(it[canonicalName])
                val event = gson.fromJson(it[values], eventClazz) as (Event)
                event.javaClass.superclass.declaredFields.filter { it.name.equals("aggregateID") }.forEach {
                    it.setAccessible(true);
                    it.set(event, aggregateID)
                }
                event.version = it[version]
                event.timestamp = it[timestamp].toGregorianCalendar().toZonedDateTime()
                result.add(event)
            }.distinct()
            return@transaction result.toList()
        }
        return IO.just(result)
    }

    override fun save(events: EventList, version: Int): IO<EventList> {
        val gson = GsonBuilder().setExclusionStrategies(CustomExclusionStrategy()).create()

        events.filter { version == 0 || it.version >= version }.forEach { event ->
            transaction {
                Events.insert {
                    it[aggregateID] = event.aggregateID
                    it[timestamp] = DateTime(event.timestamp.toInstant().toEpochMilli())
                    it[canonicalName] = event.javaClass.canonicalName
                    it[values] = gson.toJson(event)
                    it[this.version] = event.version
                } get Events.id
                commit()
            }
        }

        return IO.just(events)
    }
}