package com.hasanozgan.komandante

typealias ProjectorType = String

interface Projector {
    fun project(event: Event): DomainError
}
//
//interface Projector<A : Entity, B : Event> {
//    val projectorType: ProjectorType
//    fun project(event: B, entity: A): Try<A>
//    fun handle(event: B): Try<B>
//}
