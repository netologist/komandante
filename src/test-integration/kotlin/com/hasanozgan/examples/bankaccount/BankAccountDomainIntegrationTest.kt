package com.hasanozgan.examples.bankaccount

import arrow.effects.fix
import com.hasanozgan.examples.bankaccount.BankAccounts.balance
import com.hasanozgan.examples.bankaccount.BankAccounts.owner
import com.hasanozgan.komandante.AggregateHandler
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.commandbus.newCommandBus
import com.hasanozgan.komandante.eventbus.newEventBus
import com.hasanozgan.komandante.eventhandler.ProjectorEventHandler
import com.hasanozgan.komandante.eventhandler.SagaEventHandler
import com.hasanozgan.komandante.eventstore.exposed.dao.Events
import com.hasanozgan.komandante.eventstore.newEventStoreWithExposedAdapter
import com.hasanozgan.komandante.messagebus.newMessageBusWithLocalAdapter
import com.hasanozgan.komandante.newAggregateID
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import kotlin.test.BeforeTest
import kotlin.test.Test

class BankAccountDomainIntegrationTest {
    private val logger = LoggerFactory.getLogger(javaClass)

    val bobAccountID = newAggregateID()
    val aliceAccountID = newAggregateID()
    val messageBus = newMessageBusWithLocalAdapter()
    val commandBus = newCommandBus(messageBus)
    val eventBus = newEventBus(messageBus)
    val eventStore = newEventStoreWithExposedAdapter()
    val aggregateHandler = AggregateHandler(eventStore, eventBus)

    @BeforeTest
    fun prepare() {
        Database.connect(url = "jdbc:h2:mem:bankaccount;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Events, BankAccounts)
            commit()
        }

        // CQRS Setup
        commandBus.registerAggregate(aggregateHandler, BankAccountAggregateFactoryV2())
        commandBus.subscribe<NotificationCommand> {
            println("SAGA COMMAND: ${it}")
        }

        val bankAccountProjector = BankAccountProjectorV2()
        val projectorEventHandler = ProjectorEventHandler(bankAccountProjector, commandBus)
        eventBus.addHandler(projectorEventHandler)

        val bankAccountWorkflow = BankAccountWorkflowV2()
        val sagaEventHandler = SagaEventHandler(bankAccountWorkflow, commandBus)
        eventBus.addHandler(sagaEventHandler)

        commandBus.publish(CreateAccount(bobAccountID, "bob"))
        commandBus.publish(PerformDeposit(bobAccountID, 10.20))
        commandBus.publish(ChangeOwner(bobAccountID, "alice"))
        commandBus.publish(PerformWithdrawal(bobAccountID, 8.20))
        commandBus.publish(TransferMoney(bobAccountID, aliceAccountID, 0.10))
    }

    @Test
    fun shouldReturnEventStore4Records() {
        val expectedEventList = listOf(
                AccountCreated(bobAccountID, "bob"),
                DepositPerformed(bobAccountID, 10.20),
                OwnerChanged(bobAccountID, "alice"),
                WithdrawalPerformed(bobAccountID, 8.20),
                WithdrawalPerformed(bobAccountID, 0.10),
                MoneyTransfered(bobAccountID, aliceAccountID, 0.10)
        ).map { it as Event }
        val actualEventList = eventStore.load(bobAccountID).fix().unsafeRunSync().toList()
        assertThat(actualEventList.size, IsEqual(expectedEventList.size))
        assertThat(actualEventList, IsEqual(expectedEventList))
    }

    @Test
    fun shouldReturnBankAccountDomain() {
        transaction {
            val result = BankAccounts.select { BankAccounts.aggregateID.eq(bobAccountID) }
            assertThat(1, IsEqual(result.count()))
            result.forEach {
                assertThat("alice", IsEqual(it[owner]))
                assertThat(1.9, IsEqual(it[balance]))
            }
        }
    }
}