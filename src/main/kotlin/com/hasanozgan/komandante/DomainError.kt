package com.hasanozgan.komandante

open class DomainError(override val message: String) : Error(message)

object EventListEmptyError : DomainError("event list is empty")
object UnknownCommandError : DomainError(message = "unknown command")
//
//sealed class DomainError(val message: String, val cause: Throwable) {
//    class GeneralError(message: String, cause: Throwable) : DomainError(message, cause)
//    class NoConnectionError(message: String, cause: Throwable) : DomainError(message, cause)
//    class AuthorizationError(message: String, cause: Throwable) : DomainError(message, cause)
//}
