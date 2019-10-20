package com.hasanozgan.examples.bankaccount

import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event

sealed class BankAccountEvent(override val aggregateID: AggregateID) : Event()
data class AccountCreated(val accountID: AggregateID, val owner: String) : BankAccountEvent(accountID)
data class DepositPerformed(val accountID: AggregateID, val amount: Double) : BankAccountEvent(accountID)
data class OwnerChanged(val accountID: AggregateID, val owner: String) : BankAccountEvent(accountID)
data class WithdrawalPerformed(val accountID: AggregateID, val amount: Double) : BankAccountEvent(accountID)

sealed class NotificationEvent(override val aggregateID: AggregateID) : Event()
data class MessageSent(val messageID: AggregateID, val message: String) : NotificationEvent(messageID)