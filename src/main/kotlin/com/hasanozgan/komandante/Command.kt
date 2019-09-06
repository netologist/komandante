package com.hasanozgan.komandante

typealias CommandType = String

abstract class Command(val aggregateType: AggregateType) {
    abstract val aggregateID: AggregateID
    abstract val commandType: CommandType
}
