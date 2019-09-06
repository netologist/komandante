package com.hasanozgan.komandante

typealias ProjectorType = String

interface Projector<A : Entity, B : Event> {
    val projectorType: ProjectorType
    fun project(event: B, entity: A): Result<A, DomainError>
    fun handleEvent(event: B): Result<B, DomainError>
}
