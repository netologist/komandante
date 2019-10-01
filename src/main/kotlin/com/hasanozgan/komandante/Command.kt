package com.hasanozgan.komandante

import java.io.Serializable

typealias CommandType = String

abstract class Command():Serializable {
    abstract val aggregateID: AggregateID
}
