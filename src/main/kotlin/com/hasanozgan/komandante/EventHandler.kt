package com.hasanozgan.komandante

import arrow.core.Try

typealias EventHandlerType = String

interface EventHandler {
    val handlerType: EventHandlerType
    fun handle(event: Event): Try<Event>
}