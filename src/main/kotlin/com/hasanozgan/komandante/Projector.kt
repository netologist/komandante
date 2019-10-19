package com.hasanozgan.komandante

import arrow.core.Option

interface Projector<T : Event> {
    fun <T : Event> project(event: T): Option<Command>
}