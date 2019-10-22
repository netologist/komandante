package com.hasanozgan.komandante.commandbus

import arrow.effects.IO
import com.hasanozgan.komandante.*
import org.slf4j.LoggerFactory
import java.lang.reflect.ParameterizedType

fun newCommandBus(messageBus: MessageBus): CommandBus = CommandBusImpl(messageBus)

class CommandBusImpl(val messageBus: MessageBus) : CommandBus {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun publish(command: Command): IO<Command> {
        return messageBus.publish(command)
    }

    override fun <T : Command> subscribe(messageListener: MessageListener<in T>) {
        messageBus.subscribeOf<T>(messageListener)
    }

    override fun <T : Command> RegisterAggregate(aggregateHandler: AggregateHandler, aggregateFactory: AggregateFactory<T, *>) {
        val classTypeName = (aggregateFactory.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        val filterType = Class.forName(classTypeName.typeName)
        messageBus.subscribe(filterType, {
            CommandHandler(aggregateHandler, aggregateFactory).handle(it as Command)
        })
    }


//
//    override fun <T : Command> addHandler(filterType: Class<T>, commandHandler: CommandHandler) {
//        this.addHandler<T>(filterType, commandHandler, DefaultErrorHandler)
//    }
//
//    override fun <T : Command> addHandler(filterType: Class<T>, commandHandler: CommandHandler, onError: ErrorHandler) {
//        messageBus.subscribe(filterType, {
//            when (val result = commandHandler.handle(it as Command)) {
//                is Failure -> {
//                    logger.warn(result.exception.message)
//                }
//            }
//        }, { onError(it) })
//    }
}
