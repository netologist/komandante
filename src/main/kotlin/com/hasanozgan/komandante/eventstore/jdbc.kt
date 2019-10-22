package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.EventStore
import com.hasanozgan.komandante.eventstore.jdbc.JdbcEventStore
import javax.sql.DataSource

fun newEventStoreWithJdbcAdapter(dataSource: DataSource): EventStore {
    return JdbcEventStore(dataSource)
}