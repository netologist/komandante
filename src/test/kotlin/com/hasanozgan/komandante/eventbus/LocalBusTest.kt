package com.hasanozgan.komandante.eventbus

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.BeforeClass
import kotlin.test.Test

interface Event {}
sealed class UserEvent(userID: String) : Event

data class AddUser(val userID: String) : UserEvent(userID)
data class RemoveUser(val userID: String) : UserEvent(userID)
data class ChangeUserAddress(val userID: String) : UserEvent(userID)
data class AnotherEvent(val keyID: String) : Event

class LocalBusTest {
    companion object {
        val receivedAllEvents = mutableListOf<Event>()
        val receivedUserEvents = mutableListOf<UserEvent>()
        val receivedAnotherEvents = mutableListOf<AnotherEvent>()
        val localBus = LocalBus<Event>()
        val UserID = "123"
        val events = listOf(
                AddUser(UserID),
                RemoveUser(UserID),
                ChangeUserAddress(UserID),
                AnotherEvent("456")
        )

        @JvmStatic
        @BeforeClass
        fun prepareTest() {
            localBus.subscribe {
                receivedAllEvents.add(it)
            }

            localBus.subscribeOf<UserEvent> {
                receivedUserEvents.add(it)
            }

            localBus.subscribeOf<AnotherEvent> {
                receivedAnotherEvents.add(it)
            }

            events.forEach {
                localBus.publish(it)
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
