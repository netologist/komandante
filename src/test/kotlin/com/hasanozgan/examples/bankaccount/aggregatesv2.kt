package com.hasanozgan.examples.bankaccount

import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import com.hasanozgan.komandante.*

class BankAccountAggregateV2(override var id: AggregateID) : Aggregate() {
    var owner: String = "not/assigned"
    var balance: Double = 0.0

    fun handle(command: CreateAccount): Validated<DomainError, Event> {
        return Valid(AccountCreated(command.aggregateID, command.owner))
    }

    fun handle(command: PerformDeposit): Validated<DomainError, Event> {
        return Valid(DepositPerformed(command.aggregateID, command.amount))
    }

    fun handle(command: ChangeOwner): Validated<DomainError, Event> {
        return Valid(OwnerChanged(command.aggregateID, command.owner))
    }

    fun handle(command: PerformWithdrawal): Validated<DomainError, Event> {
        if (balance < command.amount) {
            return Invalid(InsufficientBalanceError)
        }
        return Valid(WithdrawalPerformed(command.aggregateID, command.amount))
    }

    fun apply(event: AccountCreated) {
        this.owner = event.owner
    }

    fun apply(event: OwnerChanged) {
        this.owner = event.owner
    }

    fun apply(event: DepositPerformed) {
        this.balance = this.balance.plus(event.amount)
    }

    fun apply(event: WithdrawalPerformed) {
        this.balance = this.balance.minus(event.amount)
    }
}

class BankAccountAggregateFactoryV2 : AggregateFactory<BankAccountCommand, BankAccountEvent> {
    override fun create(aggregateID: AggregateID): Aggregate {
        return BankAccountAggregateV2(aggregateID)
    }
}