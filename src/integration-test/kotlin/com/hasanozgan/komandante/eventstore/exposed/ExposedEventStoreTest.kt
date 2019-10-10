package com.hasanozgan.komandante.eventstore.exposed

import arrow.effects.fix
import com.hasanozgan.komandante.AccountCreated
import com.hasanozgan.komandante.DepositPerformed
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

        val rdbmsEventStore = createExposedEventStore()
        rdbmsEventStore.save(bobEvents, 2)
        rdbmsEventStore.save(aliceEvents, 2)

        val actualEventList = rdbmsEventStore.load(bobAccountID).fix().unsafeRunSync()
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