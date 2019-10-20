package com.hasanozgan.komandante.messagebus

import com.hasanozgan.komandante.AggregateID
import com.hasanozgan.komandante.Event
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.BeforeClass
import kotlin.test.Test

sealed class UserEvent(override val aggregateID: AggregateID) : Event()

data class AddUser(val userID: AggregateID) : UserEvent(userID)
data class RemoveUser(val userID: AggregateID) : UserEvent(userID)
data class ChangeUserAddress(val userID: AggregateID) : UserEvent(userID)
data class AnotherEvent(override val aggregateID: AggregateID) : Event()

class LocalMessageBusTest {
    companion object {
        val receivedAllEvents = mutableListOf<Event>()
        val receivedUserEvents = mutableListOf<UserEvent>()
        val receivedAnotherEvents = mutableListOf<AnotherEvent>()

        val messageBus = LocalMessageBus()
        val UserID = AggregateID.randomUUID()
        val events = listOf(
                AddUser(UserID),
                RemoveUser(UserID),
                ChangeUserAddress(UserID),
                AnotherEvent(AggregateID.randomUUID())
        )

        @JvmStatic
        @BeforeClass
        fun setup() {
            messageBus.subscribe {
                receivedAllEvents.add(it as Event)
            }

            messageBus.subscribeOf<UserEvent> {
                receivedUserEvents.add(it)
            }

            messageBus.subscribeOf<AnotherEvent> {
                receivedAnotherEvents.add(it)
            }

            events.forEach {
                messageBus.publish(it)
            }
        }
    }

    @Test
    fun shouldReceivedAllEvents() {
        assertThat(receivedAllEvents.size, IsEqual(events.size))
        assertThat(receivedAllEvents, IsEqual(events))
    }

    @Test
    fun shouldReceivedOnlyUserEvents() {
        val userEvents = events.filter { it is UserEvent }
        assertThat(receivedUserEvents.size, IsEqual(userEvents.size))
        assertThat(receivedUserEvents, IsEqual(userEvents))
    }

    @Test
    fun shouldReceivedOnlyAnotherEvents() {
        val anotherEvent = events.filter { it is AnotherEvent }
        assertThat(receivedAnotherEvents.size, IsEqual(anotherEvent.size))
        assertThat(receivedAnotherEvents, IsEqual(anotherEvent))
    }
}
