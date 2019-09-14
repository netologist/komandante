package com.hasanozgan.komandante

open class DomainError(override val message: String) : Error(message)

object EventListEmptyError : DomainError("event list is empty")
object UnknownCommandError : DomainError(message = "unknown command")

