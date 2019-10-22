package com.hasanozgan.komandante.eventstore.jdbc

import arrow.effects.IO
import com.google.gson.GsonBuilder
import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.EventList
import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.toTimestamp
import com.hasanozgan.komandante.eventstore.exposed.CustomExclusionStrategy
import com.hasanozgan.komandante.eventstore.forceUpdateAggregateID
import org.joda.time.DateTime
import java.util.*
import javax.sql.DataSource

class JdbcEventStore(val dataSource: DataSource) : EventStore {

    override fun load(aggregateID: AggregateID): IO<EventList> {
        val stmt = dataSource.getConnection().prepareStatement("SELECT DISTINCT id, aggregate_id, canonical_name, `values`, published_on, version FROM events e WHERE e.aggregate_id=? ORDER BY e.published_on")
        stmt.setString(1, aggregateID.toString())

        val result = mutableListOf<Event>()
        val gson = GsonBuilder().setExclusionStrategies(CustomExclusionStrategy()).create()

        val rs = stmt.executeQuery()
        while (rs.next()) {
            val canonicalName = rs.getString("canonical_name")
            val values = rs.getString("values")
            val timestamp = DateTime(rs.getTimestamp("published_on"))
            val eventClazz = Class.forName(canonicalName)
            val event = gson.fromJson(values, eventClazz) as (Event)
            event.version = rs.getInt("version")
            event.timestamp = timestamp.toGregorianCalendar().toZonedDateTime()
            forceUpdateAggregateID(event, aggregateID)
            result.add(event)
        }
        stmt.close()

        return IO.just(result)
    }

    override fun save(events: EventList, version: Int): IO<EventList> {
        val gson = GsonBuilder().setExclusionStrategies(CustomExclusionStrategy()).create()
        val stmt = dataSource.getConnection().prepareStatement("INSERT INTO events (id, aggregate_id, canonical_name, `values`, published_on, version) VALUES(?,?,?,?,?,?)")
        events.filter { version == 0 || it.version >= version }.forEach {
            stmt.setString(1, UUID.randomUUID().toString())
            stmt.setString(2, it.aggregateID.toString())
            stmt.setString(3, it.javaClass.canonicalName)
            stmt.setString(4, gson.toJson(it))
            stmt.setTimestamp(5, toTimestamp(it.timestamp))
            stmt.setInt(6, it.version)
            stmt.addBatch();
        }
        stmt.executeBatch()
        stmt.close()
        return IO.just(events)
    }

}