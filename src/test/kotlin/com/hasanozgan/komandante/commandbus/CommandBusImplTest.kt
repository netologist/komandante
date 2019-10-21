package com.hasanozgan.komandante.commandbus

import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.CommandHandler
import com.hasanozgan.komandante.messagebus.localMessageBus
import com.hasanozgan.komandante.newAggregateID
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.junit.BeforeClass
import org.junit.Test

sealed class UserCommand(override val aggregateID: AggregateID) : Command()

data class AddUser(val userID: AggregateID) : UserCommand(userID)
data class RemoveUser(val userID: AggregateID) : UserCommand(userID)
data class ChangeUserAddress(val userID: AggregateID) : UserCommand(userID)
data class AnotherCommand(override val aggregateID: AggregateID) : Command()


class CommandBusImplTest {
    companion object {
        val receivedAllCommands = mutableListOf<Command>()
        val receivedUserCommands = mutableListOf<UserCommand>()
        val receivedAnotherCommands = mutableListOf<AnotherCommand>()

        val messageBus = localMessageBus()
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
        val messageBus = localMessageBus()
        val commandBus = newCommandBus(messageBus)
        val userID = newAggregateID()
        val commandHandler = mockk<CommandHandler>(relaxed = true)

        commandBus.addHandler(UserCommand::class.java, commandHandler)
        commandBus.publish(AddUser(userID))

        verify { commandHandler.handle(AddUser(userID)) }
        confirmVerified(commandHandler)
    }
}
