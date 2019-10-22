package com.hasanozgan.komandante.messagebus

import arrow.effects.IO
import com.hasanozgan.komandante.ErrorHandler
import com.hasanozgan.komandante.Message
import com.hasanozgan.komandante.MessageBus
import com.hasanozgan.komandante.MessageListener
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

val DefaultErrorHandler: ErrorHandler = {}

fun newMessageBusWithLocalAdapter(): MessageBus = LocalMessageBus()

@Suppress("UNCHECKED_CAST")
class LocalMessageBus internal constructor() : MessageBus {

    private val publisher = PublishSubject.create<Message>()

    override fun <T : Message> publish(message: T): IO<T> {
        try {
            publisher.onNext(message)
            return IO.invoke { message }
        } catch (t: Throwable) {
            publisher.onError(t) // this sample for kafka bus
            return IO.raiseError(t)
        }
    }

    override fun subscribe(messageListener: MessageListener<Message>) {
        subscribe(messageListener, DefaultErrorHandler)
    }

    override fun subscribe(messageListener: MessageListener<Message>, onError: ErrorHandler) {
        val filterType = getMessageListenerClassTypeName(messageListener)
        handleMessageListenerWithTypeAndError(filterType, messageListener, onError)
    }

    override fun subscribe(filterType: Class<*>, messageListener: MessageListener<Message>) {
        subscribe(filterType, messageListener, DefaultErrorHandler)
    }

    override fun subscribe(filterType: Class<*>, messageListener: MessageListener<Message>, onError: ErrorHandler) {
        handleMessageListenerWithTypeAndError(filterType, messageListener, onError)
    }

    override fun <T : Message> subscribeOf(messageListener: MessageListener<in T>) {
        subscribeOf<T>(messageListener, DefaultErrorHandler)
    }

    override fun <T : Message> subscribeOf(messageListener: MessageListener<in T>, onError: ErrorHandler) {
        handleMessageListenerWithTypeAndError(getMessageListenerClassTypeName(messageListener), messageListener, onError)
    }

    private fun <T : Message> getMessageListenerClassTypeName(messageListener: MessageListener<T>): Class<out Any> {
        return messageListener.reflect()!!.parameters[0].type.jvmErasure.java
    }

    private fun <T : Message> handleMessageListenerWithType(filterType: Class<*>, MessageListener: MessageListener<T>) {
        publisher.ofType(filterType).subscribe { MessageListener(it as T) }
    }

    private fun <T : Message> handleMessageListenerWithTypeAndError(filterType: Class<*>, MessageListener: MessageListener<T>, onError: (error: Throwable) -> Unit) {
        publisher.ofType(filterType).subscribe({ MessageListener(it as T) }, { onError(it) })
    }
}