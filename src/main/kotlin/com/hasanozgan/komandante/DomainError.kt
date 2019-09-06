package com.hasanozgan.komandante

sealed class DomainError(override val message: String) : Error(message)

object EventListEmptyError : DomainError("event list is empty")

