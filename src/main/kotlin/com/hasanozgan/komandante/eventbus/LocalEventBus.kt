package com.hasanozgan.komandante.eventbus

import arrow.effects.IO
import com.hasanozgan.komandante.*
import io.reactivex.subjects.PublishSubject
import java.lang.reflect.ParameterizedType
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

    override fun addHandler(eventHandler: EventHandler<out T>) {
        val clazzTypeName = (eventHandler.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        val clazzType = Class.forName(clazzTypeName.typeName)
        publisher.ofType(clazzType).subscribe {
            eventHandler.handle(it as T)
        }
    }

    override fun addHandler(eventHandler: EventHandler<out T>, onError: ErrorHandler) {
        val clazzTypeName = (eventHandler.javaClass.genericInterfaces[0] as ParameterizedType).actualTypeArguments[0]
        val clazzType = Class.forName(clazzTypeName.typeName)
        publisher.ofType(clazzType).subscribe({ eventHandler.handle(it as T) }, { onError(it) })
    }
}