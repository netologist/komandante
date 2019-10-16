package com.hasanozgan.komandante

import arrow.core.Option

typealias ProjectorType = String

interface Projector<T : Event> {
    fun <T : Event> project(event: T): Option<Command>
}