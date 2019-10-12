package com.hasanozgan.komandante

typealias ProjectorType = String

interface Projector<T : Event> {
    fun <T : Event> project(event: T): DomainError?
}