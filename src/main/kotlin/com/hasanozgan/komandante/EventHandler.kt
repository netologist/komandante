package com.hasanozgan.komandante

import arrow.core.Try

typealias EventHandlerType = String

interface EventHandler<T : Event> {
    val handlerType: EventHandlerType
    fun <T : Event> handle(event: T): Try<T>
}