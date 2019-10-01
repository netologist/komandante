package com.hasanozgan.komandante.eventstore.rdbms


import arrow.data.extensions.list.foldable.exists
import arrow.effects.IO
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventList
import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.rdbms.dao.Events
import com.hasanozgan.komandante.eventstore.rdbms.dao.Events.canonicalName
import com.hasanozgan.komandante.eventstore.rdbms.dao.Events.timestamp
import com.hasanozgan.komandante.eventstore.rdbms.dao.Events.values
import com.hasanozgan.komandante.eventstore.rdbms.dao.Events.ver
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

class RdbmsEventStore : EventStore {
    override fun load(aggregateID: AggregateID): IO<EventList> {
        val result = transaction {
            val result = mutableListOf<Event>()
            val gson = GsonBuilder().setExclusionStrategies(CustomExclusionStrategy()).create()

            Events.select { Events.aggregateID.eq(aggregateID) }.map {
                val eventClazz = Class.forName(it[canonicalName])
                val event = gson.fromJson(it[values], eventClazz) as (Event)
                event.version = it[ver]
                event.timestamp = it[timestamp].toGregorianCalendar().toZonedDateTime()
                result.add(event)
            }
            return@transaction result.toList()
        }
        return IO.just(result)
    }

    // TOdo switch gson for exclusion strategy without annotation
    override fun save(events: EventList, version: Int): IO<EventList> {
        val gson = GsonBuilder().setExclusionStrategies(CustomExclusionStrategy()).create()

        val transaction = transaction {
            val result = mutableListOf<Event>()
            for (e in events) {
                val record = Events.insert {
                    it[aggregateID] = e.aggregateID
                    it[timestamp] = DateTime(e.timestamp.toInstant().toEpochMilli())
                    it[canonicalName] = e.javaClass.canonicalName
                    it[values] = gson.toJson(e)
                    it[ver] = e.version
                } get Events.id

                if (record._value != null) {
                    result.add(e)
                }
            }
            commit()

            return@transaction result.toList()
        }

        return IO.just(transaction)
    }
}