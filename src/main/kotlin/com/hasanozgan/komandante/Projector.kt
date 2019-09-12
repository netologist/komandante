package com.hasanozgan.komandante

import arrow.core.Try

typealias ProjectorType = String

interface Projector<A : Entity, B : Event> {
    val projectorType: ProjectorType
    fun project(event: B, entity: A): Try<A>
    fun handle(event: B): Try<B>
}
