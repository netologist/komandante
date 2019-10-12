package com.hasanozgan.examples.bankaccount

import com.hasanozgan.komandante.DomainError

// domain errors
object InsufficientBalanceError : DomainError(message = "insufficient balance")

