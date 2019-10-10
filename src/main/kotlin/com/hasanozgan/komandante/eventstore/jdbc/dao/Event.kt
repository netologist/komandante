package com.hasanozgan.komandante.eventstore.jdbc.dao

import java.util.*

data class Event(var id: UUID,
                 var canonicalName: String,
                 var values: String,
                 var timestamp: Long,
                 var version: String,
                 var aggregateID: UUID)
