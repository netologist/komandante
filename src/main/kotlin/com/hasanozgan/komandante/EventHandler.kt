package com.hasanozgan.komandante

typealias EventHandlerType = String

interface EventHandler {
    val handlerType: EventHandlerType
    fun handle(event: Event): Result<Event, DomainError>
}