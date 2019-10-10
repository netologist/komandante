package com.hasanozgan.komandante.eventstore.exposed.dao

import org.jetbrains.exposed.dao.UUIDTable

object Events : UUIDTable() {
    var canonicalName = varchar("canonical_name", 255)
    var values = text("values")

    var timestamp = datetime("timestamp")
    var ver = integer("version")
    var aggregateID = uuid("aggregate_id")
}