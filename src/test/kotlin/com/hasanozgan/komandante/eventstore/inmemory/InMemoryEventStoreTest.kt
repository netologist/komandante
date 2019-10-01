package com.hasanozgan.komandante.eventstore.inmemory

import arrow.effects.fix
import com.hasanozgan.komandante.AccountCreated
import com.hasanozgan.komandante.DepositPerformed
import com.hasanozgan.komandante.eventstore.createInMemoryEventStore
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

        val memoryEventStore = createInMemoryEventStore()
        memoryEventStore.save(bobEvents, 2)
        memoryEventStore.save(aliceEvents, 2)

        val actualEventList = memoryEventStore.load(bobAccountID).fix().unsafeRunSync()
        assertThat(bobEvents, IsEqual(actualEventList))
    }

    @Test
    fun shouldReturnEmptyListLoadFromEventStore() {
        val bobAccountID = newAggregateID()
        val memoryEventStore = createInMemoryEventStore()

        val actualEventList = memoryEventStore.load(bobAccountID).fix().unsafeRunSync()
        assertThat(emptyList(), IsEqual(actualEventList))
    }
}