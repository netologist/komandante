package com.hasanozgan.komandante

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
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
        logger.warn("unknown event ${event}")
        return None
    }

    open fun handle(command: Command): Validated<DomainError, List<Event>> {
        logger.warn("unknown command ${command}")
        return Valid(emptyList())
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

    fun <T : Command> invokeHandle(command: T): Validated<DomainError, List<Event>> {
        try {
            val method = getMethod(this, "handle", command) ?: return handle(command)

            val result = method.invoke(this, command)
            if (result == null) {
                return Valid(emptyList())
            }
            return when (result) {
                is DomainError -> Invalid(result)
                is Event -> Valid(listOf(result))
                is Unit -> Valid(emptyList())
                is List<*> -> Valid(result.map { it as Event })
                is Some<*> -> toValidatedType(result.t)
                is None -> Valid(emptyList())
                is Valid<*> -> toValidatedType(result.a)
                is Invalid<*> -> Invalid(toDomainError(result.e))
                else -> Invalid(UnknownCommandError)
            }
        } catch (domainError: InvocationTargetException) {
            return Invalid(DomainError(domainError.targetException.message.orEmpty()))
        } catch (domainError: InvocationTargetException) {
            return Invalid(UnknownCommandError)
        }
    }

    private fun toValidatedType(value: Any?): Validated<DomainError, List<Event>> {
        return when (value) {
            is DomainError -> return Invalid(value)
            is Event -> return Valid(listOf(value))
            is List<*> -> return Valid(value.map { it as Event })
            else -> Invalid(UnknownCommandError)
        }
    }

    private fun toDomainError(value: Any?): DomainError {
        return when (value) {
            is String -> DomainError(value)
            is DomainError -> value
            else -> UnknownCommandError
        }
    }

    fun invokeApply(event: Event): Option<DomainError> {
        val method = getMethod(this, "apply", event) ?: return apply(event)
        val result = method.invoke(this, event)

        if (result == null) {
            return None
        }
        return when (result) {
            is Some<*> -> Some(result.t as DomainError)
            else -> None
        }
    }

    fun incrementVersion() {
        version++
    }
}