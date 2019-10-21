package com.hasanozgan.komandante.eventbus

import arrow.effects.IO
import com.hasanozgan.komandante.*
import com.hasanozgan.komandante.eventhandler.ProjectorEventHandler
import com.hasanozgan.komandante.eventhandler.SagaEventHandler
import com.hasanozgan.komandante.messagebus.DefaultErrorHandler
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun newEventBus(messageBus: MessageBus): EventBus = EventBusImpl(messageBus)

class EventBusImpl internal constructor(val messageBus: MessageBus) : EventBus {
    override fun publish(message: Event): IO<Event> {
        return messageBus.publish(message)
    }

    override fun <T : Event> subscribe(messageListener: MessageListener<in T>) {
        messageBus.subscribeOf<T>(messageListener)
    }

    override fun addHandler(eventHandler: EventHandler<out Event>) {
        addHandler(eventHandler, DefaultErrorHandler)
    }

    override fun addHandler(eventHandler: EventHandler<out Event>, onError: ErrorHandler) {
        messageBus.subscribe(getEventHandlerClassTypeName(eventHandler), {
            eventHandler.handle(it as Event)
        }, { onError(it) })
    }

    private fun getEventHandlerClassTypeName(eventHandler: EventHandler<out Event>): Class<*> {
        // T type decleration name
        // val clazzTypeName = (eventHandler.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        // val genericDeclaration = (clazzTypeName as TypeVariable<*>).genericDeclaration
        // val clazzType = Class.forName(genericDeclaration)

        val clazzTypeName: Type

        if (eventHandler.handlerType == "projector") {
            val projectorEventHandler = eventHandler as ProjectorEventHandler
            clazzTypeName = (projectorEventHandler.projector.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        } else if (eventHandler.handlerType == "saga") {
            val sagaEventHandler = eventHandler as SagaEventHandler
            clazzTypeName = (sagaEventHandler.workflow.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        } else {
            clazzTypeName = (eventHandler.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        }

        return Class.forName(clazzTypeName.typeName)
    }
}