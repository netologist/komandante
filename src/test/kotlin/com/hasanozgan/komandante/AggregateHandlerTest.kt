package com.hasanozgan.komandante

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.Success
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import arrow.effects.IO
import com.hasanozgan.examples.bankaccount.*
import com.hasanozgan.komandante.eventbus.EventBus
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
        val mockEventBus = mockk<EventBus>()

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

        val aggregateFactory = BankAccountAggregateFactoryV2()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus)

        val bankAccountAggregate = aggregateFactory.create(accountID)
        val result = aggregateHandler.load(bankAccountAggregate)

        assertThat(result.isSuccess(), IsEqual(true))

        val accepted = result as Success
        val aggregate = accepted.value as BankAccountAggregateV2

        assertThat("tsubasa", IsEqual(aggregate.owner))

        // double precision problem :)
        assertThat(27.08, IsCloseTo(aggregate.balance, 0.00001))
    }

    @Test
    fun shouldAggregateHandlerSaveToEventStoreAndPublishToEventBus() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus>()

        val accountID = newAggregateID()
        val version = 0
        val events = listOf<Event>(
                AccountCreated(accountID, "totoro"),
                DepositPerformed(accountID, 20.0)
        ).mapIndexed { i, e -> e.version = i + 1; e }
        every { mockEventStore.save(events, version) } returns IO.invoke { events }
        for (event in events) {
            every { mockEventBus.publish(event) } returns IO.invoke { event }
        }

        val aggregateFactory = BankAccountAggregateFactoryV2()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus)
        val aggregate = aggregateFactory.create(accountID)
        aggregate.events = events.toMutableList()
        aggregate.version = version
        val maybeSaved = aggregateHandler.save(aggregate)

        assertTrue(maybeSaved.isSuccess())

        val bankAccount = (maybeSaved as Success).value as BankAccountAggregateV2

        assertThat("totoro", IsEqual(bankAccount.owner))
        assertThat(20.0, IsEqual(bankAccount.balance))
        assertThat(listOf(1, 2), IsEqual(bankAccount.events.map({ it.version }).toList()))
    }

    @Test
    fun shouldAggregateHandlerSaveMethodReturnADomainError() {
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus>()

        val accountID = newAggregateID()
        val version = 2
        val events = listOf<Event>(
                AccountCreated(accountID, "totoro"),
                DepositPerformed(accountID, 20.0)
        )
        every { mockEventStore.save(events, version) } returns IO.raiseError(EventListEmptyError)

        val aggregateFactory = BankAccountAggregateFactoryV2()
        val aggregateHandler = AggregateHandler(mockEventStore, mockEventBus)
        val aggregate = aggregateFactory.create(accountID)
        aggregate.events = events.toMutableList()
        aggregate.version = version
        assertTrue(aggregateHandler.save(aggregate).isFailure())
    }


    @Test
    fun shouldHandleInvokeHandlerReturnTypes() {
        // Events
        open class TestEvent(val message: String) : Event() {
            override val aggregateID: AggregateID
                get() = newAggregateID()
        }

        data class MessageEvent(val command: String) : TestEvent(command)
        data class ME01(val command: String) : TestEvent("ME01:${command}")
        data class ME02(val command: String) : TestEvent("ME02:${command}")

        // Commands
        open class TestCommand(val message: String) : Command() {
            override val aggregateID: AggregateID
                get() = newAggregateID()
        }

        data class C01(val command: String = "1") : TestCommand(command)
        data class C02(val command: String = "2") : TestCommand(command)
        data class C03(val command: String = "3") : TestCommand(command)
        data class C04(val command: String = "4") : TestCommand(command)
        data class C05(val command: String = "5") : TestCommand(command)
        data class C06(val command: String = "6") : TestCommand(command)
        data class C07(val command: String = "7") : TestCommand(command)
        data class C08(val command: String = "8") : TestCommand(command)
        data class C09(val command: String = "9") : TestCommand(command)
        data class C10(val command: String = "10") : TestCommand(command)
        data class C11(val command: String = "11") : TestCommand(command)
        data class C12(val command: String = "12") : TestCommand(command)
        data class C13(val command: String = "13") : TestCommand(command)
        data class C14(val command: String = "14") : TestCommand(command)
        data class C15(val command: String = "15") : TestCommand(command)
        data class C16(val command: String = "16") : TestCommand(command)
        data class C17(val command: String = "17") : TestCommand(command)
        data class C18(val command: String = "18") : TestCommand(command)
        data class C19(val command: String = "19") : TestCommand(command)

        val expectedCallList = mutableListOf<TestCommand>()
        val expectedErrorList = mutableListOf<String>()
        val expectedEventList = mutableListOf<String>()

        val someAggregateID = newAggregateID()
        val someAggregate = object : Aggregate() {
            override var id: AggregateID
                get() = someAggregateID
                set(value) { println(value)}

            fun handle(command: C01): Validated<DomainError, TestEvent> {
                expectedCallList.add(command)
                return Valid(MessageEvent(command.message))
            }

            fun handle(command: C02): Validated<DomainError, TestEvent> {
                expectedCallList.add(command)
                return Invalid(DomainError(command.message))
            }

            fun handle(command: C03): Validated<DomainError, List<TestEvent>> {
                expectedCallList.add(command)
                val c = command.message
                return Valid(listOf(ME01(c), ME02(c)))
            }

            fun handle(command: C04): Validated<DomainError, List<TestEvent>> {
                expectedCallList.add(command)
                return Invalid(DomainError(command.message))
            }

            fun handle(command: C05): Validated<String, TestEvent> {
                expectedCallList.add(command)
                return Valid(MessageEvent(command.message))
            }

            fun handle(command: C06): Validated<String, TestEvent> {
                expectedCallList.add(command)
                return Invalid(command.message)
            }

            fun handle(command: C07): Validated<String, List<TestEvent>> {
                expectedCallList.add(command)
                val c = command.message
                return Valid(listOf(ME01(c), ME02(c)))
            }

            fun handle(command: C08): Validated<String, List<TestEvent>> {
                expectedCallList.add(command)
                return Invalid(command.message)
            }

            fun handle(command: C09): DomainError? {
                expectedCallList.add(command)
                return DomainError(command.message)
            }

            fun handle(command: C10): DomainError? {
                expectedCallList.add(command)
                return null
            }

            fun handle(command: C11): Option<DomainError> {
                expectedCallList.add(command)
                return Some(DomainError(command.message))
            }

            fun handle(command: C12): Option<DomainError> {
                expectedCallList.add(command)
                return None
            }

            fun handle(command: C13): TestEvent? {
                expectedCallList.add(command)
                return MessageEvent(command.message)
            }

            fun handle(command: C14): TestEvent? {
                expectedCallList.add(command)
                return null
            }

            fun handle(command: C15): Option<TestEvent> {
                expectedCallList.add(command)
                return Some(MessageEvent(command.message))
            }

            fun handle(command: C16): Option<TestEvent> {
                expectedCallList.add(command)
                return None
            }

            fun handle(command: C17): List<TestEvent> {
                expectedCallList.add(command)
                val c = command.message
                return listOf(ME01(c), ME02(c))
            }

            fun handle(command: C18) {
                expectedCallList.add(command)
            }

            @Throws(DomainError::class)
            fun handle(command: C19) {
                expectedCallList.add(command)
                throw DomainError(command.message)
            }
        }

        val actualCallList = listOf<TestCommand>(C01(), C02(), C03(), C04(), C05(), C06(), C07(), C08(), C09(), C10(), C11(), C12(), C13(), C14(), C15(), C16(), C17(), C18(), C19())


        actualCallList.forEach {

            when (val result = someAggregate.invokeHandle(it)) {
                is Valid -> expectedEventList.addAll((result.a).map { (it as TestEvent).message })
                is Invalid -> expectedErrorList.add(result.e.message)
            }
        }

        assertThat(actualCallList, IsEqual(expectedCallList.toList()))
        assertThat(listOf("1", "ME01:3", "ME02:3", "5", "ME01:7", "ME02:7", "13", "15", "ME01:17", "ME02:17"), IsEqual(expectedEventList.toList()))
        assertThat(listOf("2", "4", "6", "8", "9", "11", "19"), IsEqual(expectedErrorList.toList()))
    }
}