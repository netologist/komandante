package com.hasanozgan.komandante

import arrow.core.*
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import arrow.data.extensions.list.foldable.exists
import java.lang.reflect.Method
import java.util.*

typealias AggregateID = UUID

fun newAggregateID(): AggregateID {
    return AggregateID.randomUUID()
}

abstract class Aggregate {
    abstract var id: AggregateID
    var events: MutableList<Event> = mutableListOf()
    var version: Int = 0

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
        val method = getMethod("handle", command) ?: return Invalid(UnknownCommandError)
        return when (val invoke = method.invoke(this, command)) {
            is Valid<*> -> Valid(invoke.a as Event)
            is Invalid<*> -> Invalid(invoke.e as DomainError)
            else -> Invalid(UnknownCommandError)
        }
    }

    fun invokeApply(event: Event): Option<DomainError> {
        val method = getMethod("apply", event)  ?: return Some(UnknownCommandError)

        method.invoke(this, event)
        return None
    }

    fun incrementVersion() {
        version++
    }

    private fun <T : Message> getMethod(name:String, command: T): Method? {
        return this.javaClass.methods.filter {
            it.name.equals(name) && it.parameterTypes.toList().exists {
                it.equals(command.javaClass)
            }
        }.firstOrNull()
    }

}