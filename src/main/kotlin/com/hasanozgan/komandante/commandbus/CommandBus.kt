package com.hasanozgan.komandante.commandbus

import arrow.effects.IO
import com.hasanozgan.komandante.AggregateFactory
import com.hasanozgan.komandante.AggregateHandler
import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.MessageListener

interface CommandBus {
    fun publish(command: Command): IO<Command>
    fun <T : Command> subscribe(messageListener: MessageListener<in T>)

    fun <T : Command> registerAggregate(aggregateHandler: AggregateHandler, aggregateFactory: AggregateFactory<T, *>)
}
