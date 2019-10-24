package com.hasanozgan.komandante

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.Try
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import java.util.*

typealias AggregateID = UUID

fun newAggregateID(): AggregateID {
    return AggregateID.randomUUID()
}

abstract class Aggregate {
    abstract var id: AggregateID
    var events: MutableList<Event> = mutableListOf()
    var version: Int = 0

    open fun apply(event: Event): Option<DomainError> {
        return Some(UnknownEventError)
    }

    open fun handle(command: Command): Validated<DomainError, Event> {
        return Invalid(UnknownCommandError)
    }

    fun store(event: Event): Try<Event> {
        return when (val applied = invokeApply(event)) {
            is Some -> Try.raiseError(applied.t)
            is None -> {
                incrementVersion()
                event.version = version
                events.add(event);
                Try.just(event)
            }
        }
    }

    fun <T : Command> invokeHandle(command: T): Validated<DomainError, Event> {
        val method = getMethod(this, "handle", command) ?: return handle(command)
        return when (val result = method.invoke(this, command)) {
            is Valid<*> -> Valid(result.a as Event)
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