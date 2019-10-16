package com.hasanozgan.komandante

import arrow.core.Success
import arrow.effects.IO
import com.hasanozgan.examples.bankaccount.*
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.number.IsCloseTo
import org.junit.Test
import kotlin.test.assertTrue

class AggregateHandlerTest {
    @Test
    fun shouldAggregateHandlerLoadFromEventStore() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus<Event>>()

        val accountID = newAggregateID()
        every { mockEventStore.load(accountID) } returns IO.invoke {
            listOf<Event>(
                    AccountCreated(accountID, "totoro"),
                    DepositPerformed(accountID, 20.0),
                    DepositPerformed(accountID, 15.20),
                    OwnerChanged(accountID, "tsubasa"),
                    WithdrawalPerformed(accountID, 8.12)
            )
        }

        val aggregateFactory = BankAccountAggregateFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)

        val result = aggregateHandler.load(accountID)

        assertThat(result.isSuccess(), IsEqual(true))

        val accepted = result as Success
        val aggregate = accepted.value as BankAccountAggregate

        assertThat("tsubasa", IsEqual(aggregate.owner))

        // double precision problem :)
        assertThat(27.08, IsCloseTo(aggregate.balance, 0.00001))
    }

    @Test
    fun shouldAggregateHandlerSaveToEventStoreAndPublishToEventBus() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus<out Event>>()

        val accountID = newAggregateID()
        val version = 0
        val events = listOf<Event>(
                AccountCreated(accountID, "totoro"),
                DepositPerformed(accountID, 20.0)
        ).mapIndexed { i, e -> e.version=i+1; e }
        every { mockEventStore.save(events, version) } returns IO.invoke { events }
        for (event in events) {
            every { mockEventBus.publish(event) } returns IO.invoke { event }
        }

        val aggregateFactory = BankAccountAggregateFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)
        val aggregate = aggregateFactory.create(accountID)
        aggregate.events = events.toMutableList()
        aggregate.version = version
        val maybeSaved = aggregateHandler.save(aggregate)

        assertTrue(maybeSaved.isSuccess())

        val bankAccount = (maybeSaved as Success).value as BankAccountAggregate

        assertThat("totoro", IsEqual(bankAccount.owner))
        assertThat(20.0, IsEqual(bankAccount.balance))
        assertThat(listOf(1, 2), IsEqual(bankAccount.events.map({ it.version }).toList()))
    }

    @Test
    fun shouldAggregateHandlerSaveMethodReturnADomainError() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus<Event>>()

        val accountID = newAggregateID()
        val version = 2
        val events = listOf<Event>(
                AccountCreated(accountID, "totoro"),
                DepositPerformed(accountID, 20.0)
        )
        every { mockEventStore.save(events, version) } returns IO.raiseError(EventListEmptyError)

        val aggregateFactory = BankAccountAggregateFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)
        val aggregate = aggregateFactory.create(accountID)
        aggregate.events = events.toMutableList()
        aggregate.version = version
        assertTrue(aggregateHandler.save(aggregate).isFailure())
    }
}