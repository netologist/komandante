package com.hasanozgan.komandante.eventstore.jdbc

import arrow.effects.IO
import com.google.gson.GsonBuilder
import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventList
import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.exposed.CustomExclusionStrategy
import org.joda.time.DateTime
import java.util.*
import javax.sql.DataSource

class JdbcEventStore(val dataSource: DataSource) : EventStore {

    override fun load(aggregateID: AggregateID): IO<EventList> {
        val stmt = dataSource.getConnection().prepareStatement("SELECT id, aggregate_id, canonical_name, `values`, `timestamp`, version FROM events e WHERE e.aggregate_id=? ORDER BY e.timestamp")
        stmt.setString(1, aggregateID.toString())

        val result = mutableListOf<Event>()
        val gson = GsonBuilder().setExclusionStrategies(CustomExclusionStrategy()).create()

        val rs = stmt.executeQuery()
        while (rs.next()) {
            val canonicalName = rs.getString("canonical_name")
            val values = rs.getString("values")
            val timestamp = DateTime(rs.getLong("timestamp"))
            val eventClazz = Class.forName(canonicalName)

            val event = gson.fromJson(values, eventClazz) as (Event)
            event.version = rs.getInt("version")
            event.timestamp = timestamp.toGregorianCalendar().toZonedDateTime()

            result.add(event)
        }
        stmt.close()

        return IO.just(result)
    }

    override fun save(events: EventList, version: Int): IO<EventList> {
        val gson = GsonBuilder().setExclusionStrategies(CustomExclusionStrategy()).create()
        val stmt = dataSource.getConnection().prepareStatement("INSERT INTO events (id, aggregate_id, canonical_name, `values`, `timestamp`, version) VALUES(?,?,?,?,?,?)")
        events.forEach {
            stmt.setString(1, UUID.randomUUID().toString())
            stmt.setString(2, it.aggregateID.toString())
            stmt.setString(3, it.javaClass.canonicalName)
            stmt.setString(4, gson.toJson(it))
            stmt.setLong(5, it.timestamp.toInstant().toEpochMilli())
            stmt.setInt(6, it.version)
            stmt.addBatch();
        }
        stmt.executeBatch()
        stmt.close()
        return IO.just(events)
    }

}