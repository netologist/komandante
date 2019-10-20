package com.hasanozgan.komandante

abstract class Command() : Message {
    abstract val aggregateID: AggregateID
}
