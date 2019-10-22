package com.hasanozgan.komandante

interface AggregateFactory<C : Command, E : Event> {
    fun create(aggregateID: AggregateID): Aggregate
}