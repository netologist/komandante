package com.hasanozgan.examples.bankaccount

import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Command

sealed class BankAccountCommand(override val aggregateID: AggregateID) : Command()
data class CreateAccount(val accountID: AggregateID, val owner: String) : BankAccountCommand(accountID)
data class ChangeOwner(val accountID: AggregateID, val owner: String) : BankAccountCommand(accountID)
data class PerformDeposit(val accountID: AggregateID, val amount: Double) : BankAccountCommand(accountID)
data class PerformWithdrawal(val accountID: AggregateID, val amount: Double) : BankAccountCommand(accountID)
