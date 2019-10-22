[![Maintainability](https://api.codeclimate.com/v1/badges/f6f81a8ab5b6b1070f33/maintainability)](https://codeclimate.com/github/hasanozgan/komandante/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/f6f81a8ab5b6b1070f33/test_coverage)](https://codeclimate.com/github/hasanozgan/komandante/test_coverage)
[![CircleCI](https://circleci.com/gh/hasanozgan/komandante.svg?style=svg)](https://circleci.com/gh/hasanozgan/komandante)

# komandante

## (under development, not ready for production)

Komandante is CQRS / ES toolkit

## CQRS / ES Setup

### Main Setup
```kotlin
val messageBus = localMessageBus()
val commandBus = newCommandBus(messageBus)
val eventBus = newEventBus(messageBus)
val eventStore = createExposedEventStore()
val aggregateHandler = AggregateHandler(eventStore, eventBus)
val commandBus = newCommandBus(messageBus)

commandBus.registerAggregateFactory(BankAccountAggregateFactory())
commandBus.subscribe<NotificationCommand> {
    println("SAGA COMMAND: ${it}")
}

val bankAccountProjector = BankAccountProjector()
val projectorEventHandler = ProjectorEventHandler(bankAccountProjector, commandBus)
eventBus.addHandler(projectorEventHandler)

val bankAccountWorkflow = BankAccountWorkflow()
val sagaEventHandler = SagaEventHandler(bankAccountWorkflow, commandBus)
eventBus.addHandler(sagaEventHandler)
```


### Commands
```kotlin
sealed class BankAccountCommand(override val aggregateID: AggregateID) : Command()
data class CreateAccount(val accountID: AggregateID, val owner: String) : BankAccountCommand(accountID)
data class ChangeOwner(val accountID: AggregateID, val owner: String) : BankAccountCommand(accountID)
data class PerformDeposit(val accountID: AggregateID, val amount: Double) : BankAccountCommand(accountID)
data class PerformWithdrawal(val accountID: AggregateID, val amount: Double) : BankAccountCommand(accountID)

sealed class NotificationCommand() : Command() {
    override val aggregateID: AggregateID
        get() = newAggregateID()
}
data class SendMessage(val message: String) : NotificationCommand()
```

### Events
```kotlin
sealed class BankAccountEvent(override val aggregateID: AggregateID) : Event()
data class AccountCreated(val accountID: AggregateID, val owner: String) : BankAccountEvent(accountID)
data class DepositPerformed(val accountID: AggregateID, val amount: Double) : BankAccountEvent(accountID)
data class OwnerChanged(val accountID: AggregateID, val owner: String) : BankAccountEvent(accountID)
data class WithdrawalPerformed(val accountID: AggregateID, val amount: Double) : BankAccountEvent(accountID)

sealed class NotificationEvent(override val aggregateID: AggregateID) : Event()
data class MessageSent(val messageID: AggregateID, val message: String) : NotificationEvent(messageID)
```

### Root Aggregate

```kotlin
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
```
### Root Aggregate Factory
```kotlin
class BankAccountAggregateFactory : AggregateFactory<BankAccountCommand, BankAccountEvent> {
    override fun create(aggregateID: AggregateID): Aggregate {
        return BankAccountAggregate(aggregateID)
    }
}
```
