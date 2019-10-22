package com.hasanozgan.komandante

import arrow.core.Failure
import arrow.core.Success
import arrow.core.Try
import arrow.data.extensions.list.foldable.foldLeft
import arrow.effects.extensions.io.applicativeError.handleError
import arrow.effects.extensions.io.monad.binding
import arrow.effects.fix
import com.hasanozgan.komandante.eventbus.EventBus

class AggregateHandler(private val store: EventStore, private val bus: EventBus) {
    fun load(aggregate: Aggregate): Try<Aggregate> {
        return binding {
            val (events) = store.load(aggregate.id)
            events.map { event -> aggregate.store(event) }.foldLeft(Try { aggregate }, { _, c ->
                return@foldLeft when (c) {
                    is Failure -> Failure(c.exception)
                    is Success -> {
                        Success(aggregate)
                    }
                }
            })
        }.handleError {
            Failure(it)
        }.fix().unsafeRunSync()
    }

    fun save(aggregate: Aggregate): Try<Aggregate> {
        return binding {
            val (events) = store.save(aggregate.events, aggregate.version)
            applyEvents(aggregate, events).map {
                it.events.filter { it.version >= aggregate.version }.forEach { bus.publish(it) }
                it
            }
        }.handleError { Failure(it) }.fix().unsafeRunSync()
    }

    private fun applyEvents(aggregate: Aggregate, events: EventList): Try<Aggregate> {
        return events.map { event -> aggregate.apply(event) }.foldLeft(Try { aggregate }, { _, c ->
            return@foldLeft when (c) {
                is Failure -> Failure(c.exception)
                is Success -> Success(aggregate)
            }
        })
    }
}