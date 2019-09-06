package com.hasanozgan.komandante

sealed class Result<out A, out R>()
data class Accept<A>(val value: A) : Result<A, Nothing>()
data class Reject<R>(val reason: R) : Result<Nothing, R>()

fun <A, R> Result<A, R>.isAccepted(): Boolean {
    if (this is Accept) return true
    return false
}

fun <A, R> Result<A, R>.isRejected(): Boolean {
    if (this is Reject) return true
    return false
}
