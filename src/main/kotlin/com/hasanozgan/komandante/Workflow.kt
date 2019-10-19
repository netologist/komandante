package com.hasanozgan.komandante

interface Workflow<T : Event> {
    fun <T : Event> run(event: T): List<Command>
}