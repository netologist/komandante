package com.hasanozgan.komandante

typealias EventHandlerType = String

interface EventHandler<T : Event> {
    val handlerType: EventHandlerType
    fun <T : Event> handle(event: T)
}