package com.hasanozgan.komandante.eventbus

//import java.lang.reflect.Class
import arrow.effects.IO
import com.hasanozgan.komandante.*
import com.hasanozgan.komandante.eventhandler.ProjectorEventHandler
import io.reactivex.subjects.PublishSubject
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

// localBus constructor
fun <T : Event> localBusOf(): LocalEventBus<T> = LocalEventBus()

@Suppress("UNCHECKED_CAST")
class LocalEventBus<T : Event> internal constructor() : EventBus<T> {

    private val publisher = PublishSubject.create<T>()

    override fun publish(event: T): IO<T> {
        try {
            publisher.onNext(event)
            return IO.invoke { event }
        } catch (t: Throwable) {
            publisher.onError(t) // this sample for kafka bus
            return IO.raiseError(t)
        }
    }

    override fun <K> publish(event: K): IO<K> {
        return publish(event as T).map { it as K }
    }


    override fun subscribe(eventListener: EventListener<T>) {
        val clazzType = eventListener.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe {
            eventListener(it as T)
        }
    }

    override fun subscribe(eventListener: EventListener<T>, onError: (error: Throwable) -> Unit) {
        val clazzType = eventListener.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe({ eventListener(it as T) }, { onError(it) })
    }


    override fun <T> subscribeOf(eventListener: EventListener<in T>) {
        val clazzType = eventListener.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe {
            eventListener(it as T)
        }
    }

    override fun <T> subscribeOf(eventListener: EventListener<in T>, onError: ErrorHandler) {
        val clazzType = eventListener.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe({ eventListener(it as (T)) }, { onError(it) })
    }

    override fun addHandler(eventHandler: EventHandler<out Event>) {
        val clazzType = getEventHandlerClassTypeName(eventHandler)
        publisher.ofType(clazzType).subscribe {
            eventHandler.handle(it as Event)
        }
    }

    override fun addHandler(eventHandler: EventHandler<out Event>, onError: ErrorHandler) {
        val clazzType = getEventHandlerClassTypeName(eventHandler)
        publisher.ofType(clazzType).subscribe({ eventHandler.handle(it as Event) }, { onError(it) })
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
        } else {
            clazzTypeName = (eventHandler.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        }

        return Class.forName(clazzTypeName.typeName)
    }
}