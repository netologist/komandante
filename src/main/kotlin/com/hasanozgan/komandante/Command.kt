package com.hasanozgan.komandante

import java.io.Serializable

typealias CommandType = String

abstract class Command() {
    abstract val aggregateID: AggregateID
}
