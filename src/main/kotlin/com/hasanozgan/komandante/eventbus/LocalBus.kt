package com.hasanozgan.komandante.eventbus

import arrow.effects.IO
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

// localBus constructor
fun <T : Any> localBusOf(): LocalBus<T> = LocalBus()

@Suppress("UNCHECKED_CAST")
class LocalBus<T : Any> internal constructor() : EventBus<T> {

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

    override fun subscribe(eventHandler: EventHandler<T>) {
        val clazzType = eventHandler.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe {
            eventHandler(it as (T))
        }
    }

    override fun subscribe(eventHandler: EventHandler<T>, onError: (error: Throwable) -> Unit) {
        val clazzType = eventHandler.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe({ eventHandler(it as (T)) }, { onError(it) })
    }


    override fun <T> subscribeOf(eventHandler: EventHandler<in T>) {
        val clazzType = eventHandler.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe {
            eventHandler(it as (T))
        }
    }

    override fun <T> subscribeOf(eventHandler: EventHandler<in T>, onError: ErrorHandler) {
        val clazzType = eventHandler.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe({ eventHandler(it as (T)) }, { onError(it) })
    }
}