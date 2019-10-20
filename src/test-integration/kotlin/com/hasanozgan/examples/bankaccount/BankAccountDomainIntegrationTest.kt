package com.hasanozgan.examples.bankaccount

import arrow.effects.fix
import com.hasanozgan.examples.bankaccount.BankAccounts.balance
import com.hasanozgan.examples.bankaccount.BankAccounts.owner
import com.hasanozgan.komandante.AggregateHandler
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.commandbus.localCommandBus
import com.hasanozgan.komandante.eventbus.localEventBus
import com.hasanozgan.komandante.eventhandler.ProjectorEventHandler
import com.hasanozgan.komandante.eventhandler.SagaEventHandler
import com.hasanozgan.komandante.eventstore.createExposedEventStore
import com.hasanozgan.komandante.eventstore.exposed.dao.Events
import com.hasanozgan.komandante.newAggregateID
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test

class BankAccountDomainIntegrationTest {
    val accountID = newAggregateID()
    val eventStore = createExposedEventStore()
    val eventBus = localEventBus()
    val commandBus = localCommandBus()

    @BeforeTest
    fun prepare() {
        Database.connect(url = "jdbc:h2:mem:bankaccount;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Events, BankAccounts)
            commit()
        }

        // CQRS Setup
        val bankAccountAggregateHandler = AggregateHandler(eventStore, eventBus, BankAccountAggregateFactory())
        commandBus.addHandler(bankAccountAggregateHandler, BankAccountCommand::class.java)
        commandBus.subscribeOf<NotificationCommand> {
            println("SAGA COMMAND: ${it}")
        }

        val bankAccountProjector = BankAccountProjector()
        val projectorEventHandler = ProjectorEventHandler(bankAccountProjector, commandBus)
        eventBus.addHandler(projectorEventHandler)

        val bankAccountWorkflow = BankAccountWorkflow()
        val sagaEventHandler = SagaEventHandler(bankAccountWorkflow, commandBus)
        eventBus.addHandler(sagaEventHandler)

        commandBus.publish(CreateAccount(accountID, "bob"))
        commandBus.publish(PerformDeposit(accountID, 10.20))
        commandBus.publish(ChangeOwner(accountID, "alice"))
        commandBus.publish(PerformWithdrawal(accountID, 8.20))
    }

    @Test
    fun shouldReturnEventStore4Records() {
        val expectedEventList = listOf(
                AccountCreated(accountID, "bob"),
                DepositPerformed(accountID, 10.20),
                OwnerChanged(accountID, "alice"),
                WithdrawalPerformed(accountID, 8.20)
        ).map { it as Event }
        val actualEventList = eventStore.load(accountID).fix().unsafeRunSync().toList()
        assertThat(4, IsEqual(expectedEventList.size))
        assertThat(actualEventList, IsEqual(expectedEventList))
    }

    @Test
    fun shouldReturnBankAccountDomain() {
        transaction {
            val result = BankAccounts.select { BankAccounts.aggregateID.eq(accountID) }
            assertThat(1, IsEqual(result.count()))
            result.forEach {
                assertThat("alice", IsEqual(it[owner]))
                assertThat(2.0, IsEqual(it[balance]))
            }
        }
    }
}