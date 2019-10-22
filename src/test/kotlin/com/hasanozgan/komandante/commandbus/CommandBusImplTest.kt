package com.hasanozgan.komandante.commandbus

import com.hasanozgan.examples.bankaccount.BankAccountAggregate
import com.hasanozgan.komandante.*
import com.hasanozgan.komandante.messagebus.newMessageBusWithLocalAdapter
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.junit.BeforeClass
import org.junit.Test

sealed class UserEvent(override val aggregateID: AggregateID) : Event()
sealed class UserCommand(override val aggregateID: AggregateID) : Command()

data class AddUser(val userID: AggregateID) : UserCommand(userID)
data class RemoveUser(val userID: AggregateID) : UserCommand(userID)
data class ChangeUserAddress(val userID: AggregateID) : UserCommand(userID)
data class AnotherCommand(override val aggregateID: AggregateID) : Command()

class UserAggregateFactory : AggregateFactory<UserCommand, UserEvent> {
    override fun create(aggregateID: AggregateID): Aggregate {
        return BankAccountAggregate(aggregateID)
    }
}

class CommandBusImplTest {
    companion object {
        val receivedAllCommands = mutableListOf<Command>()
        val receivedUserCommands = mutableListOf<UserCommand>()
        val receivedAnotherCommands = mutableListOf<AnotherCommand>()

        val messageBus = newMessageBusWithLocalAdapter()
        val aggregateHandler = mockk<AggregateHandler>()
        val commandBus = newCommandBus(messageBus)
        val UserID = AggregateID.randomUUID()
        val commands = listOf(
                AddUser(UserID),
                RemoveUser(UserID),
                ChangeUserAddress(UserID),
                AnotherCommand(AggregateID.randomUUID())
        )

        @JvmStatic
        @BeforeClass
        fun setup() {
            commandBus.subscribe<Command> {
                receivedAllCommands.add(it)
            }

            commandBus.subscribe<UserCommand> {
                receivedUserCommands.add(it)
            }

            commandBus.subscribe<AnotherCommand> {
                receivedAnotherCommands.add(it)
            }

            commands.forEach {
                commandBus.publish(it)
            }
        }
    }

    @Test
    fun shouldReceivedAllCommands() {
        MatcherAssert.assertThat(receivedAllCommands.size, IsEqual(commands.size))
        MatcherAssert.assertThat(receivedAllCommands, IsEqual(commands))
    }

    @Test
    fun shouldReceivedOnlyUserCommands() {
        val userEvents = commands.filter { it is UserCommand }
        MatcherAssert.assertThat(receivedUserCommands.size, IsEqual(userEvents.size))
        MatcherAssert.assertThat(receivedUserCommands, IsEqual(userEvents))
    }

    @Test
    fun shouldReceivedOnlyAnotherCommands() {
        val anotherCommand = commands.filter { it is AnotherCommand }
        MatcherAssert.assertThat(receivedAnotherCommands.size, IsEqual(anotherCommand.size))
        MatcherAssert.assertThat(receivedAnotherCommands, IsEqual(anotherCommand))
    }

    @Test
    fun shouldAddCommandHandler() {

        val messageBus = newMessageBusWithLocalAdapter()
        val commandBus = newCommandBus(messageBus)
        val userID = newAggregateID()
        val aggregateHandler = mockk<AggregateHandler>(relaxed = true)
        val aggregateFactory = mockk<UserAggregateFactory>(relaxed = true)

        commandBus.registerAggregate(aggregateHandler, aggregateFactory)
        commandBus.publish(AddUser(userID))

        verify { aggregateFactory.create(userID) }
        confirmVerified(aggregateFactory)
    }
}
