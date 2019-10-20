package com.hasanozgan.komandante.commandbus

import arrow.effects.IO
import com.hasanozgan.komandante.AggregateHandler
import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.CommandBus
import com.hasanozgan.komandante.CommandHandlerError
import io.reactivex.subjects.PublishSubject

fun localBusOf(): CommandBus = LocalCommandBus()

@Suppress("UNCHECKED_CAST")
class LocalCommandBus : CommandBus {

    private val publisher = PublishSubject.create<Command>()

    override fun publish(command: Command): IO<Command> {
        try {
            publisher.onNext(command)
            return IO.invoke { command }
        } catch (t: Throwable) {
            publisher.onError(t) // this sample for kafka bus
            return IO.raiseError(t)
        }
    }

    override fun addHandler(aggregateHandler: AggregateHandler) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addHandler(aggregateHandler: AggregateHandler, onError: CommandHandlerError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Command> addHandlerOf(clazz: Class<T>, aggregateHandler: AggregateHandler) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Command> addHandlerOf(clazz: Class<T>, aggregateHandler: AggregateHandler, onError: CommandHandlerError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
