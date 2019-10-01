package com.hasanozgan.komandante

import arrow.core.Success
import arrow.core.Try
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated

class BankAccount(override var id: AggregateID) : Aggregate {
    var owner: String = "not/assigned"
    var balance: Double = 0.0

    override var events: MutableList<Event> = mutableListOf()
    override var version: Int = 0

    override fun apply(event: Event): Try<Event> {
        when (event) {
            is AccountCreated -> {
                this.owner = event.owner
            }
            is DepositPerformed ->
                this.balance = this.balance.plus(event.amount)
            is OwnerChanged -> {
                this.owner = event.owner
            }
            is WithdrawalPerformed ->
                this.balance = this.balance.minus(event.amount)
        }
        return Success(event)
    }

    override fun handle(command: Command): Validated<DomainError, Event> {
        return when (command) {
            is CreateAccount ->
                Valid(AccountCreated(command.aggregateID, command.owner))
            is PerformDeposit ->
                return Valid(DepositPerformed(command.aggregateID, command.amount))
            is ChangeOwner ->
                Valid(OwnerChanged(command.aggregateID, command.owner))
            is PerformWithdrawal -> {
                if (balance < command.amount) {
                    return Invalid(InsufficientBalanceError)
                }
                Valid(WithdrawalPerformed(command.aggregateID, command.amount))
            }
            else -> Invalid(UnknownCommandError)
        }
    }
}

class BankAccountFactory : AggregateFactory {
    override fun create(aggregateID: AggregateID): Aggregate {
        return BankAccount(aggregateID)
    }
}

sealed class BankAccountCommand(override val aggregateID: AggregateID) : Command()
data class CreateAccount(val accountID: AggregateID, val owner: String) : BankAccountCommand(accountID)
data class ChangeOwner(val accountID: AggregateID, val owner: String) : BankAccountCommand(accountID)
data class PerformDeposit(val accountID: AggregateID, val amount: Double) : BankAccountCommand(accountID)
data class PerformWithdrawal(val accountID: AggregateID, val amount: Double) : BankAccountCommand(accountID)

sealed class BankAccountEvent(override val aggregateID: AggregateID) : Event()
data class AccountCreated(val accountID: AggregateID, val owner: String) : BankAccountEvent(accountID)
data class DepositPerformed(val accountID: AggregateID, val amount: Double) : BankAccountEvent(accountID)
data class OwnerChanged(val accountID: AggregateID, val owner: String) : BankAccountEvent(accountID)
data class WithdrawalPerformed(val accountID: AggregateID, val amount: Double) : BankAccountEvent(accountID)

// domain errors
object InsufficientBalanceError : DomainError(message = "insufficient balance")

