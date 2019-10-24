package com.hasanozgan.komandante

abstract class Workflow<T : Event> {
    fun <T : Event> invokeRun(event: T): List<Command> {
        val method = getMethod(this, "run", event) ?: return run(event)
        return when (val result = method.invoke(this, event)) {
            is List<*> -> result.map { it as Command }
            else -> emptyList()
        }
    }

    open fun run(event: Event): List<Command> {
        return emptyList()
    }
}