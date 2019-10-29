package com.hasanozgan.komandante

import arrow.core.Failure
import arrow.effects.IO
import com.hasanozgan.examples.bankaccount.*
import com.hasanozgan.komandante.eventbus.EventBus
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Test

class CommandHandlerTest {
    @Test
    fun shouldCommandHandlerInvokeAggregateHandlerMethod() {
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

        val aggregateFactory = BankAccountAggregateFactoryV2()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus)
        val commandHandler = CommandHandler(aggregateHandler, aggregateFactory)
        commandHandler.handle(PerformDeposit(accountID, 15.20))
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

        val aggregateFactory = BankAccountAggregateFactoryV2()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus)
        val commandHandler = CommandHandler(aggregateHandler, aggregateFactory)
        commandHandler.handle(PerformWithdrawal(accountID, 20.10))
    }

    @Test
    fun shouldNothingEventStoreLoadMethodReturnEmptyList() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus>()

        every { mockEventStore.load(any()) } returns IO.invoke { listOf<Event>() }

        val aggregateFactory = BankAccountAggregateFactoryV2()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus)
        val commandHandler = CommandHandler(aggregateHandler, aggregateFactory)
        commandHandler.handle(SendMessage("some command message"))
    }
}