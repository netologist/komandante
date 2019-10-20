package com.hasanozgan.komandante

import arrow.core.Failure
import arrow.core.Success
import arrow.effects.IO
import com.hasanozgan.examples.bankaccount.*
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Test
import kotlin.test.assertTrue

class CommandHandlerTest {
    @Test
    fun shouldCommandHandlerReturnAnEvent() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus>()

        val accountID = newAggregateID()
        every { mockEventStore.load(accountID) } returns IO.invoke {
            listOf<Event>(
                    AccountCreated(accountID, "totoro"),
                    DepositPerformed(accountID, 20.0)
            )
        }

        every { mockEventStore.save(listOf(AccountCreated(accountID, "totoro"), DepositPerformed(accountID, 20.0), DepositPerformed(accountID, 15.20)), 3) } returns IO.invoke { emptyList<Event>() }

        every { mockEventBus.publish(AccountCreated(accountID, "totoro")) } returns IO.invoke { AccountCreated(accountID, "totoro") }
        every { mockEventBus.publish(DepositPerformed(accountID, 20.0)) } returns IO.invoke { DepositPerformed(accountID, 20.0) }
        every { mockEventBus.publish(DepositPerformed(accountID, 15.20)) } returns IO.invoke { DepositPerformed(accountID, 15.20) }

        val aggregateFactory = BankAccountAggregateFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)
        val commandHandler = CommandHandler<BankAccountCommand>(aggregateHandler)
        val maybeEvent = commandHandler.handle(PerformDeposit(accountID, 15.20))

        assertTrue(maybeEvent.isSuccess())
        assertThat(DepositPerformed(accountID, 15.20), IsEqual((maybeEvent as Success).value))
    }

    @Test
    fun shouldCommandHandlerReturnAError() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus>()

        val accountID = newAggregateID()
        every { mockEventStore.load(accountID) } returns IO.invoke {
            listOf<Event>(
                    AccountCreated(accountID, "totoro"),
                    DepositPerformed(accountID, 20.0)
            )
        }

        val aggregateFactory = BankAccountAggregateFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)
        val commandHandler = CommandHandler<BankAccountCommand>(aggregateHandler)
        val maybeEvent = commandHandler.handle(PerformWithdrawal(accountID, 20.10))

        assertTrue(maybeEvent.isFailure())
        assertThat(InsufficientBalanceError, IsEqual((maybeEvent as Failure).exception))
    }

    @Test
    fun shouldCommandHandlerReturnAErrorInvalidCommand() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus>()

        every { mockEventStore.load(any()) } returns IO.invoke { listOf<Event>() }

        val aggregateFactory = BankAccountAggregateFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)
        val commandHandler = CommandHandler<BankAccountCommand>(aggregateHandler)
        val maybeEvent = commandHandler.handle(SendMessage("some command message"))

        assertTrue(maybeEvent.isFailure())
        assertThat(UnknownCommandError, IsEqual((maybeEvent as Failure).exception))
    }
}