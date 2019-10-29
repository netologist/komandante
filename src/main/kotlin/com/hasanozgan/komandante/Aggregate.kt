package com.hasanozgan.komandante

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import org.slf4j.LoggerFactory
import java.util.*

typealias AggregateID = UUID

fun newAggregateID(): AggregateID {
    return AggregateID.randomUUID()
}

abstract class Aggregate {
    private val logger = LoggerFactory.getLogger(javaClass)

    abstract var id: AggregateID
    var events: MutableList<Event> = mutableListOf()
    var version: Int = 0

    open fun apply(event: Event): Option<DomainError> {
        return Some(UnknownEventError)
    }

    open fun handle(command: Command): Validated<DomainError, Event> {
        return Invalid(UnknownCommandError)
    }

    fun store(event: Event) {
        when (val applied = invokeApply(event)) {
            is Some -> logger.error(applied.t.message)
            is None -> {
                incrementVersion()
                event.version = version
                events.add(event)
            }
        }
    }

    //TODO: New Version requirement (CREATE: a task)
    // version 2 requirement except throw error and handle in your code.
    // expected return types prototoypes
    // fun handle(event: Event): Validated<DomainError, Event>
    // fun handle(event: Event): Validated<DomainError, List<Event>>
    // fun handle(event: Event): Validated<String, Event>
    // fun handle(event: Event): Validated<String, List<Event>>
    // fun handle(event: Event): DomainError?
    // fun handle(event: Event): Option<DomainError>
    // fun handle(event: Event): Event?
    // fun handle(event: Event): Option<Event>
    // fun handle(event: Event): List<Event>
    // fun handle(event: Event): throw error
    // fun handle(event: Event): // ( empty, no return type )

    fun <T : Command> invokeHandle(command: T): Validated<DomainError, List<Event>> {

        val method = getMethod(this, "handle", command) ?: return handle(command).map { listOf(it) }
        return when (val result = method.invoke(this, command)) {
            is Valid<*> -> Valid(listOf(result.a as Event))
            is Invalid<*> -> Invalid(result.e as DomainError)
            else -> Invalid(UnknownCommandError)
        }
    }

    fun invokeApply(event: Event): Option<DomainError> {
        val method = getMethod(this, "apply", event) ?: return apply(event)

        return when (val result = method.invoke(this, event)) {
            is Some<*> -> Some(result.t as DomainError)
            else -> None
        }
    }

    fun incrementVersion() {
        version++
    }
}