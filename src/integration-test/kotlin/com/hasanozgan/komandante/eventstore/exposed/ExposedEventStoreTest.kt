package com.hasanozgan.komandante.eventstore.exposed

import arrow.effects.fix
import com.hasanozgan.examples.bankaccount.AccountCreated
import com.hasanozgan.examples.bankaccount.DepositPerformed
import com.hasanozgan.komandante.eventstore.createExposedEventStore
import com.hasanozgan.komandante.eventstore.exposed.dao.Events
import com.hasanozgan.komandante.newAggregateID
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import kotlin.test.BeforeTest

class ExposedEventStoreTest {
    @BeforeTest
    fun prepare() {
        Database.connect("jdbc:h2:mem:exposed_test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Events)
            commit()
        }
    }

    @Test
    fun shouldLoadAndSaveFromEventStore() {
        val bobAccountID = newAggregateID()
        val aliceAccountID = newAggregateID()

        val bobEvents = listOf(AccountCreated(bobAccountID, "bob"), DepositPerformed(bobAccountID, 20.0))
        val aliceEvents = listOf(AccountCreated(aliceAccountID, "alice"), DepositPerformed(aliceAccountID, 15.8))

        val exposedEventStore = createExposedEventStore()
        exposedEventStore.save(bobEvents, 0)
        exposedEventStore.save(aliceEvents, 0)

        val actualEventList = exposedEventStore.load(bobAccountID).fix().unsafeRunSync()
        assertThat(bobEvents, IsEqual(actualEventList))
    }

    @Test
    fun shouldReturnEmptyListLoadFromEventStore() {
        val bobAccountID = newAggregateID()
        val rdbmsEventStore = createExposedEventStore()

        val actualEventList = rdbmsEventStore.load(bobAccountID).fix().unsafeRunSync()
        assertThat(emptyList(), IsEqual(actualEventList))
    }
}