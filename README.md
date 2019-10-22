[![Maintainability](https://api.codeclimate.com/v1/badges/f6f81a8ab5b6b1070f33/maintainability)](https://codeclimate.com/github/hasanozgan/komandante/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/f6f81a8ab5b6b1070f33/test_coverage)](https://codeclimate.com/github/hasanozgan/komandante/test_coverage)
[![CircleCI](https://circleci.com/gh/hasanozgan/komandante.svg?style=svg)](https://circleci.com/gh/hasanozgan/komandante)

# komandante

## (under development, not ready for production)

Komandante is CQRS / ES toolkit

## CQRS / ES Setup

### Main Setup
```kotlin
val messageBus = newMessageBusWithLocalAdapter()
val commandBus = newCommandBus(messageBus)
val eventBus = newEventBus(messageBus)
val eventStore = newEventStoreWithExposedAdapter()
val aggregateHandler = AggregateHandler(eventStore, eventBus)

commandBus.registerAggregate(aggregateHandler, BankAccountAggregateFactory())
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

### Projector Event Handler
```kotlin
class BankAccountProjector : Projector<BankAccountEvent> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun <T : Event> project(event: T): Option<Command> {
        transaction {
            val repository = BankAccounts.select { aggregateID.eq(event.aggregateID) }

            when (event) {
                is AccountCreated ->
                    if (repository.empty()) {
                        BankAccounts.insert {
                            it[aggregateID] = event.aggregateID
                            it[owner] = event.owner
                            it[balance] = 0.0
                            it[updatedOn] = DateTime.now()
                            it[version] = event.version
                        }
                        commit()
                    } else {
                        println(DomainError("account is created before"))
                    }


                is DepositPerformed ->
                    repository
                            .filterNot {
                                it[version] >= event.version
                            }
                            .forEach { row ->
                                BankAccounts.update({ aggregateID.eq(event.aggregateID) }, 1, {
                                    it[balance] = row[balance].plus(event.amount)
                                    it[updatedOn] = DateTime.now()
                                    it[version] = event.version
                                })
                                commit()
                            }


                is OwnerChanged ->
                    repository
                            .filterNot {
                                it[version] >= event.version
                            }
                            .forEach {
                                BankAccounts.update({ aggregateID.eq(event.aggregateID) }, 1, {
                                    it[owner] = event.owner
                                    it[updatedOn] = DateTime.now()
                                    it[version] = event.version
                                })
                                commit()
                            }


                is WithdrawalPerformed ->
                    repository
                            .filterNot {
                                it[version] >= event.version
                            }
                            .forEach { row ->
                                BankAccounts.update({ aggregateID.eq(event.aggregateID) }, 1, {
                                    it[balance] = row[balance].minus(event.amount)
                                    it[updatedOn] = DateTime.now()
                                    it[version] = event.version
                                })
                                commit()
                            }

                else ->
                    println(DomainError("Event ${event} is not projected"))
            }
        }
        return None
    }
}
```

### Saga (Workflow) Event Handler
```kotlin
class BankAccountWorkflow : Workflow<BankAccountEvent> {
    override fun <T : Event> run(event: T): List<Command> {
        when (event) {
            is AccountCreated ->
                return listOf(SendMessage("account created ${event.owner} for ${event.aggregateID}"))

            is DepositPerformed ->
                return listOf(SendMessage("${event.amount} deposit performed for ${event.aggregateID}"))

            is OwnerChanged ->
                return listOf(SendMessage("${event.owner} owner changed for ${event.aggregateID}"))

            is WithdrawalPerformed ->
                return listOf(SendMessage("${event.amount} withdrawal performed for ${event.aggregateID}"))
        }
        return emptyList()
    }
}
```