package com.hasanozgan.komandante.eventstore.rdbms.dao

import org.jetbrains.exposed.dao.UUIDTable
import java.time.ZonedDateTime

object Events: UUIDTable() {
    val canonicalName = varchar("canonical_name", 255)
    val values = text("values")

    var timestamp = datetime("timestamp")
    var ver = integer("version")
    var aggregateID = uuid("aggregate_id")
}