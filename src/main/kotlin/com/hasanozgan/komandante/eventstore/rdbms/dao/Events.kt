package com.hasanozgan.komandante.eventstore.rdbms.dao

import org.jetbrains.exposed.dao.UUIDTable
import java.time.ZonedDateTime

object Events: UUIDTable() {
    val objectName = varchar("object_name", 255)
    val values = text("values")

    var timestamp = datetime("timestamp")
    var version = integer("version")
    var aggregateID = uuid("aggregate_id")
}