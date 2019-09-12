package com.hasanozgan.komandante.eventstore.inmemory

import arrow.effects.extensions.io.monad.binding
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
        val aliceEvents = listOf(AccountCreated(aliceAccountID, "alice"), DepositPerformed(bobAccountID, 15.8))

        val memoryEventStore = createInMemoryEventStore()
        memoryEventStore.save(bobEvents, 2)
        memoryEventStore.save(aliceEvents, 2)

        binding {
            val (expectedEventList) = memoryEventStore.load(bobAccountID)
            assertThat(emptyList(), IsEqual(expectedEventList))
        }
    }

    @Test
    fun shouldReturnEmptyListLoadFromEventStore() {
        val bobAccountID = newAggregateID()
        val memoryEventStore = createInMemoryEventStore()

        binding {
            val (expectedEventList) = memoryEventStore.load(bobAccountID)
            assertThat(emptyList(), IsEqual(expectedEventList))
        }
    }
}