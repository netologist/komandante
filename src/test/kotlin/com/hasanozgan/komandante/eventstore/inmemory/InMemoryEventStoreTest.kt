package com.hasanozgan.komandante.eventstore.inmemory

import arrow.effects.fix
import com.hasanozgan.examples.bankaccount.AccountCreated
import com.hasanozgan.examples.bankaccount.DepositPerformed
import com.hasanozgan.komandante.eventstore.newEventStoreWithInMemoryAdapter
import com.hasanozgan.komandante.newAggregateID
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Test

class InMemoryEventStoreTest {
    @Test
    fun shouldLoadAndSaveFromEventStore() {
        val bobAccountID = newAggregateID()
        val aliceAccountID = newAggregateID()

        val bobEvents = listOf(AccountCreated(bobAccountID, "bob"), DepositPerformed(bobAccountID, 20.0))
        val aliceEvents = listOf(AccountCreated(aliceAccountID, "alice"), DepositPerformed(aliceAccountID, 15.8))

        val memoryEventStore = newEventStoreWithInMemoryAdapter()
        memoryEventStore.save(bobEvents, 2)
        memoryEventStore.save(aliceEvents, 2)

        val actualEventList = memoryEventStore.load(bobAccountID).fix().unsafeRunSync()
        assertThat(bobEvents, IsEqual(actualEventList))
    }

    @Test
    fun shouldReturnEmptyListLoadFromEventStore() {
        val bobAccountID = newAggregateID()
        val memoryEventStore = newEventStoreWithInMemoryAdapter()

        val actualEventList = memoryEventStore.load(bobAccountID).fix().unsafeRunSync()
        assertThat(emptyList(), IsEqual(actualEventList))
    }
}