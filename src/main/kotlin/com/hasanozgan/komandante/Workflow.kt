package com.hasanozgan.komandante

typealias WorkflowType = String

interface Workflow<T : Event> {
    fun <T : Event> run(event: T): List<Command>
}