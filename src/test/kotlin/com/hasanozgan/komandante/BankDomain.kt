package com.hasanozgan.komandante

import arrow.core.Success
import arrow.core.Try

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

    override fun handle(command: Command): Try<Event> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

sealed class BankAccountEvent() : Event()
data class AccountCreated(override val aggregateID: AggregateID, val owner: String) : BankAccountEvent()
data class DepositPerformed(override val aggregateID: AggregateID, val amount: Double) : BankAccountEvent()
data class OwnerChanged(override val aggregateID: AggregateID, val owner: String) : BankAccountEvent()
data class WithdrawalPerformed(override val aggregateID: AggregateID, val amount: Double) : BankAccountEvent()
