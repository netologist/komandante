package com.hasanozgan.komandante.eventstore

import com.hasanozgan.komandante.*
import com.hasanozgan.komandante.eventstore.jpa.Created
import com.hasanozgan.komandante.eventstore.jpa.Deleted
import com.hasanozgan.komandante.eventstore.jpa.TodoListAggregateType
import com.hasanozgan.komandante.eventstore.jpa.TodoListEvent
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Test
import java.util.*
//
//sealed class TodoListEvent(override val eventType: EventType) : Event() {
//    override val aggregateType: AggregateType = TodoListAggregateType
//}
//
//data class Created(override val aggregateID: AggregateID) : TodoListEvent("todolist:created")
//data class Deleted(override val aggregateID: AggregateID) : TodoListEvent("todolist:deleted")

class MemoryEventStoreTest {
    val todolistID = newAggregateID()
//
//    @Test
//    fun shouldGetSavedEventsFromStore() {
//        val actualEvents = listOf(Created(todolistID), Deleted(todolistID))
//        val anotherEvents = listOf(Created(UUID.randomUUID()), Deleted(UUID.randomUUID()))
//        val memoryEventStore = createInMemoryEventStore()
//        memoryEventStore.save(actualEvents, 0)
//        memoryEventStore.save(anotherEvents, 0)
//
//        val exceptedEvents = memoryEventStore.load(todolistID)
//        assertThat(actualEvents, IsEqual(exceptedEvents))
//    }
}