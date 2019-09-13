package com.hasanozgan.komandante

typealias CommandType = String

abstract class Command {
    abstract val aggregateID: AggregateID
}
