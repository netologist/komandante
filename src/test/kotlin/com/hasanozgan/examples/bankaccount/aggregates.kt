package com.hasanozgan.examples.bankaccount

import arrow.core.Success
import arrow.core.Try
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import com.hasanozgan.komandante.*

class BankAccountAggregate(override var id: AggregateID) : Aggregate {
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

class BankAccountAggregateFactory : AggregateFactory<BankAccountCommand, BankAccountEvent> {
    override fun create(aggregateID: AggregateID): Aggregate {
        return BankAccountAggregate(aggregateID)
    }
}