package com.hasanozgan.komandante.eventbus

import com.hasanozgan.examples.bankaccount.MessageSent
import com.hasanozgan.examples.bankaccount.NotificationEvent
import com.hasanozgan.komandante.*
import com.hasanozgan.komandante.messagebus.localMessageBus
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.BeforeClass
import org.junit.Test

sealed class UserEvent(override val aggregateID: AggregateID) : Event()

data class UserAdded(val userID: AggregateID) : UserEvent(userID)
data class UserRemoved(val userID: AggregateID) : UserEvent(userID)
data class UserAddressChanged(val userID: AggregateID) : UserEvent(userID)
data class AnotherEventCalled(override val aggregateID: AggregateID) : Event()

class EventBusImplTest {
    companion object {
        val receivedAllEvents = mutableListOf<Event>()
        val receivedUserEvents = mutableListOf<UserEvent>()
        val receivedAnotherEvents = mutableListOf<AnotherEventCalled>()
        val receivedEventHandlerEvents = mutableListOf<Event>()

        val messageBus = localMessageBus()
        val eventBus = newEventBus(messageBus)
        val UserID = AggregateID.randomUUID()
        val events = listOf(
                UserAdded(UserID),
                UserRemoved(UserID),
                UserAddressChanged(UserID),
                AnotherEventCalled(AggregateID.randomUUID())
        )

        @JvmStatic
        @BeforeClass
        fun setup() {
            eventBus.subscribe<Event> {
                receivedAllEvents.add(it)
            }

            eventBus.subscribe<UserEvent> {
                receivedUserEvents.add(it)
            }

            eventBus.subscribe<AnotherEventCalled> {
                receivedAnotherEvents.add(it)
            }

            val eventHandler = object : EventHandler<UserEvent> {
                override fun <T : Event> handle(event: T) {
                    receivedEventHandlerEvents.add(event)
                }

                override val handlerType: EventHandlerType
                    get() = "test-event-handler"
            }
            eventBus.addHandler(eventHandler)

            events.forEach {
                eventBus.publish(it)
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
        val anotherEvent = events.filter { it is AnotherEventCalled }
        assertThat(receivedAnotherEvents.size, IsEqual(anotherEvent.size))
        assertThat(receivedAnotherEvents, IsEqual(anotherEvent))
    }

    @Test
    fun shouldAddEventHandler() {
        val userEvents = events.filter { it is UserEvent }
        assertThat(receivedEventHandlerEvents.size, IsEqual(userEvents.size))
        assertThat(receivedEventHandlerEvents, IsEqual(userEvents))
    }

    @Test
    fun shouldAddEventHandlerWithError() {
        val messageBus = localMessageBus()
        val localBus = newEventBus(messageBus)
        val dummyException = Exception("dummy exception")
        localBus.addHandler(object : EventHandler<NotificationEvent> {
            override val handlerType: EventHandlerType
                get() = "dummy-handler"

            override fun <T : Event> handle(event: T) {
                throw dummyException
            }
        }, { assertThat(dummyException, IsEqual(it)) })
        localBus.publish(MessageSent(newAggregateID(), "some message"))
    }
}
