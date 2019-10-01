package com.hasanozgan.komandante.eventstore.rdbms.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*

class Event(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Event>(Events)

    var objectName by Events.canonicalName
    var values by Events.values
    var timestamp = Events.timestamp
    var ver = Events.ver
    var aggregateID = Events.aggregateID
}