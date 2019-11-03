package com.hasanozgan.examples.bankaccount

import arrow.core.None
import arrow.core.Option
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import com.hasanozgan.komandante.*

class BankAccountAggregateV1(override var id: AggregateID) : Aggregate() {
    var owner: String = "not/assigned"
    var balance: Double = 0.0

    override fun apply(event: Event): Option<DomainError> {
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
        return None
    }

    override fun handle(command: Command): Validated<DomainError, List<Event>> {
        return when (command) {
            is CreateAccount ->
                Valid(listOf(AccountCreated(command.aggregateID, command.owner)))
            is PerformDeposit ->
                return Valid(listOf(DepositPerformed(command.aggregateID, command.amount)))
            is ChangeOwner ->
                Valid(listOf(OwnerChanged(command.aggregateID, command.owner)))
            is PerformWithdrawal -> {
                if (balance < command.amount) {
                    return Invalid(InsufficientBalanceError)
                }
                Valid(listOf(WithdrawalPerformed(command.aggregateID, command.amount)))
            }
            else -> Invalid(UnknownCommandError)
        }
    }
}

class BankAccountAggregateFactoryV1 : AggregateFactory<BankAccountCommand, BankAccountEvent> {
    override fun create(aggregateID: AggregateID): Aggregate {
        return BankAccountAggregateV1(aggregateID)
    }
}