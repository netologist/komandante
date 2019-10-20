package com.hasanozgan.komandante.eventstore.exposed.dao

import org.jetbrains.exposed.dao.UUIDTable

object Events : UUIDTable() {
    var canonicalName = varchar("canonical_name", 255)
    var values = text("values")

    var publishedOn = datetime("published_on")
    var version = integer("version")
    var aggregateID = uuid("aggregate_id")
}