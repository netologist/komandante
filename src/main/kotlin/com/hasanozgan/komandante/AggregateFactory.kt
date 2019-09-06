package com.hasanozgan.komandante

interface AggregateFactory {
    fun create(aggregateID: AggregateID): Aggregate
}