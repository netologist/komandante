package com.hasanozgan.komandante.commandbus

import arrow.effects.IO
import com.hasanozgan.komandante.*
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

fun localCommandBus(): CommandBus = LocalCommandBus()

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

    override fun subscribe(commandListener: CommandListener<Command>) {
        publisher.ofType(Command::class.java).subscribe {
            commandListener(it)
        }
    }

    override fun subscribe(commandListener: CommandListener<Command>, onError: CommandHandlerError) {
        publisher.ofType(getCommandListenerClassTypeName(commandListener))
                .subscribe({ commandListener(it as Command) }, { onError(it) })
    }

    override fun <T : Command> subscribeOf(commandListener: CommandListener<in T>) {
        publisher.ofType(getCommandListenerClassTypeName(commandListener)).subscribe { commandListener(it as T) }
    }

    override fun <T : Command> subscribeOf(commandListener: CommandListener<in T>, onError: CommandHandlerError) {
        publisher.ofType(getCommandListenerClassTypeName(commandListener))
                .subscribe({ commandListener(it as T) }, { onError(it) })
    }

    override fun addHandler(aggregateHandler: AggregateHandler) {
        publisher.ofType(Command::class.java).subscribe {
            CommandHandler(aggregateHandler).handle(it)
        }
    }

    override fun addHandler(aggregateHandler: AggregateHandler, onError: CommandHandlerError) {
        publisher.ofType(Command::class.java).subscribe({
            CommandHandler(aggregateHandler).handle(it)
        }, {
            onError(it)
        })
    }

    override fun <T : Command> addHandler(aggregateHandler: AggregateHandler, baseCommandClass: Class<T>, onError: CommandHandlerError) {
        publisher.ofType(baseCommandClass).subscribe({
            CommandHandler(aggregateHandler).handle(it)
        }, {
            onError(it)
        })
    }

    override fun <T : Command> addHandler(aggregateHandler: AggregateHandler, baseCommandClass: Class<T>) {
        publisher.ofType(baseCommandClass).subscribe {
            CommandHandler(aggregateHandler).handle(it)
        }
    }

    private fun <T> getCommandListenerClassTypeName(commandListener: CommandListener<T>): Class<out Any> {
        val clazzType = commandListener.reflect()!!.parameters[0].type.jvmErasure.java
        return clazzType
    }

}
