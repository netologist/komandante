package com.hasanozgan.examples.bankaccount

import arrow.core.None
import arrow.core.Option
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

    fun handle(command: PerformDeposit): Event {
        return DepositPerformed(command.aggregateID, command.amount)
    }

    fun handle(command: ChangeOwner): Event {
        return OwnerChanged(command.aggregateID, command.owner)
    }

    fun handle(command: TransferMoney): Validated<DomainError, List<Event>> {
        if (balance < command.amount) {
            return Invalid(InsufficientBalanceError)
        }

        return Valid(listOf(
                WithdrawalPerformed(command.from, command.amount),
                WithdrawalPerformed(command.to, command.amount),
                MoneyTransfered(command.from, command.to, command.amount))
        )
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

    fun apply(event: DepositPerformed): Option<DomainError> {
        this.balance = this.balance.plus(event.amount)
        return None
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