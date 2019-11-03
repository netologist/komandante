package com.hasanozgan.komandante

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.none

abstract class Projector<T : Event> {
    fun <E : Event> invokeProject(event: E): Option<Command> {
        val method = getMethod(this, "project", event) ?: return project(event)

        val result = method.invoke(this, event)
        if (result == null) {
            return None
        }
        return when (result) {
            is Some<*> -> Some(result.t as Command)
            else -> None
        }
    }

    open fun project(event: Event): Option<Command> {
        return none()
    }
}
