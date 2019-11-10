[![Maintainability](https://api.codeclimate.com/v1/badges/f6f81a8ab5b6b1070f33/maintainability)](https://codeclimate.com/github/hasanozgan/komandante/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/f6f81a8ab5b6b1070f33/test_coverage)](https://codeclimate.com/github/hasanozgan/komandante/test_coverage)
[![CircleCI](https://circleci.com/gh/hasanozgan/komandante.svg?style=svg)](https://circleci.com/gh/hasanozgan/komandante)

# komandante

## (under development, not ready for production)

_**Komandante is CQRS / ES toolkit** inspired by [Eventhorizon](https://github.com/looplab/eventhorizon) and [Eventhus](https://github.com/mishudark/eventhus)_

### Example todo-mvc app
`kotlin` `ktor` `exposed` `komandante`

https://github.com/hasanozgan/komandante-todomvc


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
class BankAccountAggregate(override var id: AggregateID) : Aggregate() {
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

    override fun project(event: Event): Option<Command> {
        println(DomainError("Event ${event} is not projected"))
        return none()
    }

    fun project(event: AccountCreated) {
        transaction {
            val query = BankAccounts.select { aggregateID.eq(event.aggregateID) }
            if (query.empty()) {
                BankAccounts.insert {
                    it[aggregateID] = event.aggregateID
                    it[owner] = event.owner
                    it[balance] = 0.0
                    it[updatedOn] = DateTime.now()
                    it[version] = event.version
                }
                commit()
            } else {
                logger.error("account is created before")
            }
        }
    }

    fun project(event: DepositPerformed) {
        transaction {
            BankAccounts.select { aggregateID.eq(event.aggregateID) }
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
        }
    }

    fun project(event: OwnerChanged) {
        transaction {
            BankAccounts.select { aggregateID.eq(event.aggregateID) }
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
        }
    }

    fun project(event: WithdrawalPerformed): Option<Command> {
        transaction {
            BankAccounts.select { aggregateID.eq(event.aggregateID) }
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
        }

        return none()
    }
}
```

### Saga (Workflow) Event Handler
```kotlin
class BankAccountWorkflow : Workflow<BankAccountEvent> {
    fun run(event: AccountCreated): List<Command> {
        return listOf(SendMessage("account created ${event.owner} for ${event.aggregateID}"))
    }

    fun run(event: DepositPerformed): List<Command> {
        return listOf(SendMessage("${event.amount} deposit performed for ${event.aggregateID}"))
    }

    fun run(event: OwnerChanged): List<Command> {
        return listOf(SendMessage("${event.owner} owner changed for ${event.aggregateID}"))
    }

    fun run(event: WithdrawalPerformed): List<Command> {
        return listOf(SendMessage("${event.amount} withdrawal performed for ${event.aggregateID}"))
    }
}
```
