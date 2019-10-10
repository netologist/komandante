package com.hasanozgan.komandante

import arrow.core.Failure
import arrow.core.Success
import arrow.core.Try
import arrow.data.extensions.list.foldable.foldLeft
import arrow.effects.extensions.io.applicativeError.handleError
import arrow.effects.extensions.io.monad.binding
import arrow.effects.fix

class AggregateHandler(private val store: EventStore, private val bus: EventBus<Event>, private val aggregateFactory: AggregateFactory) {
    fun load(aggregateID: AggregateID): Try<Aggregate> {
        val aggregate = aggregateFactory.create(aggregateID)

        return binding {
            val (events) = store.load(aggregateID)
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
            events.map { event -> aggregate.apply(event) }.foldLeft(Try { aggregate }, { _, c ->
                return@foldLeft when (c) {
                    is Failure -> Failure(c.exception)
                    is Success -> Success(aggregate)
                }
            }).map {
                it.events.map {
                    val (event) = bus.publish(it)
                    event.version = events.indexOf(event) + 1
                    event
                }
                it
            }
        }.handleError {
            Failure(it)
        }.fix().unsafeRunSync()
    }
}