package com.hasanozgan.komandante.eventbus

import io.reactivex.subjects.PublishSubject
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

// localBus constructor
fun <T> localBusOf(): LocalBus<T> = LocalBus()

@Suppress("UNCHECKED_CAST")
class LocalBus<T> internal constructor() : EventBus<T> {
    private val publisher = PublishSubject.create<T>()

    override fun publish(event: T) {
        publisher.onNext(event)
    }

    override fun subscribe(eventHandler: EventHandler<T>) {
        val clazzType = eventHandler.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe {
            eventHandler(it as (T))
        }
    }

    override fun <T> subscribeOf(eventHandler: EventHandler<in T>) {
        val clazzType = eventHandler.reflect()!!.parameters[0].type.jvmErasure.java
        publisher.ofType(clazzType).subscribe {
            eventHandler(it as (T))
        }
    }
}