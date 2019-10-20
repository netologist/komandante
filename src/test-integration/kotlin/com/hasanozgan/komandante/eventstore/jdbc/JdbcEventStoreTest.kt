package com.hasanozgan.komandante.eventstore.jdbc

import arrow.effects.fix
import com.hasanozgan.examples.bankaccount.AccountCreated
import com.hasanozgan.examples.bankaccount.DepositPerformed
import com.hasanozgan.komandante.eventstore.createJdbcEventStore
import com.hasanozgan.komandante.newAggregateID
import com.zaxxer.hikari.HikariDataSource
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.junit.Test
import javax.sql.DataSource
import kotlin.test.BeforeTest

class JdbcEventStoreTest {
    lateinit var datasource: DataSource

    fun createTestDataSource(): DataSource {
        val hikariDataSource = HikariDataSource()
        hikariDataSource.driverClassName = "org.h2.Driver"
        hikariDataSource.jdbcUrl = "jdbc:h2:mem:jdbc_test;DB_CLOSE_DELAY=-1"
        hikariDataSource.maximumPoolSize = 30

        return hikariDataSource
    }

    fun createSchema() {
        datasource = createTestDataSource()
        val stmt = datasource.getConnection().createStatement()

        stmt.addBatch("""
            DROP TABLE events IF EXISTS;
        """.trimIndent())

        stmt.addBatch("""
            CREATE TABLE events (
              id VARCHAR(36) NOT NULL, 
              aggregate_id VARCHAR(36) NOT NULL,
              canonical_name VARCHAR(255) NOT NULL,
              `values` TEXT NOT NULL,
              published_on TIMESTAMP NOT NULL,
              version NUMBER NOT NULL 
            );
        """.trimIndent())

        stmt.executeBatch()
        stmt.close()
    }

    @BeforeTest
    fun prepare() {
        createSchema()
    }

    @Test
    fun shouldLoadAndSaveFromEventStore() {
        val bobAccountID = newAggregateID()
        val aliceAccountID = newAggregateID()

        val bobEvents = listOf(AccountCreated(bobAccountID, "bob"), DepositPerformed(bobAccountID, 20.0))
        val aliceEvents = listOf(AccountCreated(aliceAccountID, "alice"), DepositPerformed(aliceAccountID, 15.8))

        val jdbcEventStore = createJdbcEventStore(datasource)
        jdbcEventStore.save(bobEvents.mapIndexed { i, e ->
            e.version = i + 1;
            e
        }, 0)
        jdbcEventStore.save(aliceEvents, 0)

        val actualEventList = jdbcEventStore.load(bobAccountID).fix().unsafeRunSync()
        MatcherAssert.assertThat(bobEvents, IsEqual(actualEventList))
    }

    @Test
    fun shouldReturnEmptyListLoadFromEventStore() {
        val bobAccountID = newAggregateID()
        val rdbmsEventStore = createJdbcEventStore(datasource)

        val actualEventList = rdbmsEventStore.load(bobAccountID).fix().unsafeRunSync()
        MatcherAssert.assertThat(emptyList(), IsEqual(actualEventList))
    }
}