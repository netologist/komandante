package com.hasanozgan.komandante.commandbus

import com.hasanozgan.examples.bankaccount.BankAccountCommand
import com.hasanozgan.komandante.AggregateFactory
import com.hasanozgan.komandante.AggregateHandler
import com.hasanozgan.komandante.EventBus
import com.hasanozgan.komandante.EventStore
import io.mockk.mockk
import org.junit.Test


class LocalCommandBusTest {
    @Test
    fun testA() {
        // TODO: Add localCommandBus test
        val commandBus = localBusOf()
        val mockEventStore = mockk<EventStore>()
        val mockEventBus = mockk<EventBus>()
        val factory = mockk<AggregateFactory>()

        commandBus.addHandlerOf(BankAccountCommand::class.java, AggregateHandler(mockEventStore, mockEventBus, factory))
    }
}