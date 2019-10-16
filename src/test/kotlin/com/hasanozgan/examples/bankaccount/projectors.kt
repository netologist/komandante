package com.hasanozgan.examples.bankaccount

import arrow.core.None
import arrow.core.Option
import com.hasanozgan.examples.bankaccount.BankAccounts.aggregateID
import com.hasanozgan.examples.bankaccount.BankAccounts.version
import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.DomainError
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.Projector
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

class BankAccountProjector : Projector<BankAccountEvent> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun <T : Event> project(event: T): Option<Command> {
        transaction {
            val repository = BankAccounts.select { aggregateID.eq(event.aggregateID) }

            when (event) {
                is AccountCreated ->
                    if (repository.empty()) {
                        BankAccounts.insert {
                            it[aggregateID] = event.aggregateID
                            it[owner] = event.owner
                            it[balance] = 0.0
                            it[updatedOn] = DateTime.now()
                            it[version] = event.version
                        }
                        commit()
                    } else {
                        println( DomainError("account is created before"))
                    }


                is DepositPerformed ->
                    repository
                            .filterNot {
                                it[version] >= event.version
                            }
                            .forEach { row ->
                                BankAccounts.update({ aggregateID.eq(event.aggregateID) }, 1, {
                                    it[balance] = row[balance].plus(event.amount)
                                    it[updatedOn] = DateTime.now()
                                    it[version] = event.version
                                })
                                commit()
                            }


                is OwnerChanged ->
                    repository
                            .filterNot {
                                it[version] >= event.version
                            }
                            .forEach {
                                BankAccounts.update({ aggregateID.eq(event.aggregateID) }, 1, {
                                    it[owner] = event.owner
                                    it[updatedOn] = DateTime.now()
                                    it[version] = event.version
                                })
                                commit()
                            }


                is WithdrawalPerformed ->
                    repository
                            .filterNot {
                                it[version] >= event.version
                            }
                            .forEach { row ->
                                BankAccounts.update({ aggregateID.eq(event.aggregateID) }, 1, {
                                    it[balance] = row[balance].minus(event.amount)
                                    it[updatedOn] = DateTime.now()
                                    it[version] = event.version
                                })
                                commit()
                            }

                else ->
                    println(DomainError("Event ${event} is not projected"))
            }
        }
        return None
    }
}