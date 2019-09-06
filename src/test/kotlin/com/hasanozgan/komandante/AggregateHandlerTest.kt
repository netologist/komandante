package com.hasanozgan.komandante

import arrow.core.None
import arrow.core.Some
import com.hasanozgan.komandante.eventbus.EventBus
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.number.IsCloseTo
import org.junit.Test

class AggregateHandlerTest {
    @Test
    fun shouldAggregateHandlerLoadFromEventStore() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus<Event>>()

        val accountID = newAggregateID()
        every { mockEventStore.load(accountID) } returns listOf<Event>(
                AccountCreated(accountID, "totoro"),
                DepositPerformed(accountID, 20.0),
                DepositPerformed(accountID, 15.20),
                OwnerChanged(accountID, "tsubasa"),
                WithdrawalPerformed(accountID, 8.12)
        )

        val aggregateFactory = BankAccountFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)

        val result = aggregateHandler.load(accountID)
        assertThat(result.isAccepted(), IsEqual(true))

        val accepted     = result as Accept
        val aggregate = accepted.value as BankAccount

        assertThat("tsubasa", IsEqual(aggregate.owner))

        // double precision problem :)
        assertThat(27.08, IsCloseTo(aggregate.balance, 0.00001))
    }

    @Test
    fun shouldAggregateHandlerSaveToEventStoreAndPublishToEventBus() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus<Event>>()

        val accountID = newAggregateID()
        val version = 2
        val events = listOf<Event>(
                AccountCreated(accountID, "totoro"),
                DepositPerformed(accountID, 20.0)
        )
        every { mockEventStore.save(events, version) } returns None
        for (event in events) {
            every { mockEventBus.publish(event) } returns Unit
        }

        val aggregateFactory = BankAccountFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)
        val aggregate = aggregateFactory.create(accountID)
        aggregate.events = events.toMutableList()
        aggregate.version = version
        val maybeSaved = aggregateHandler.save(aggregate)

        assertThat(None, IsEqual(maybeSaved))
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
        every { mockEventStore.save(events, version) } returns Some(EventListEmptyError)

        val aggregateFactory = BankAccountFactory()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus, aggregateFactory)
        val aggregate = aggregateFactory.create(accountID)
        aggregate.events = events.toMutableList()
        aggregate.version = version
        val maybeSaved = aggregateHandler.save(aggregate)
        when (maybeSaved) {
            is Some -> {
                assertThat(EventListEmptyError, IsEqual(maybeSaved.t))
            }
        }
    }
}