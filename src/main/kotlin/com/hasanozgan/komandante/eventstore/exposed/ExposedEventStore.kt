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
import com.hasanozgan.komandante.eventstore.exposed.dao.Events.publishedOn
import com.hasanozgan.komandante.eventstore.exposed.dao.Events.values
import com.hasanozgan.komandante.eventstore.exposed.dao.Events.version
import com.hasanozgan.komandante.eventstore.forceUpdateAggregateID
import com.hasanozgan.komandante.eventstore.toDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
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

            Events.select { Events.aggregateID.eq(aggregateID) }.sortedBy { publishedOn }.map {
                val eventClazz = Class.forName(it[canonicalName])
                val event = gson.fromJson(it[values], eventClazz) as (Event)
                event.version = it[version]
                event.timestamp = it[publishedOn].toGregorianCalendar().toZonedDateTime()
                forceUpdateAggregateID(event, aggregateID)
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
                    it[publishedOn] = toDateTime(event.timestamp)
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