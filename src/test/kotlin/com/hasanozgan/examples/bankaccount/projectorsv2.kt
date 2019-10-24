package com.hasanozgan.examples.bankaccount

import arrow.core.Option
import arrow.core.none
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

class BankAccountProjectorV2 : Projector<BankAccountEvent>() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun project(event: Event): Option<Command> {
        println(DomainError("Event ${event} is not projected"))
        return none()
    }

    fun project(event: AccountCreated) {
        transaction {
            val query = BankAccounts.select { aggregateID.eq(event.aggregateID) }
            if (query.empty()) {
                BankAccounts.insert {
                    it[aggregateID] = event.aggregateID
                    it[owner] = event.owner
                    it[balance] = 0.0
                    it[updatedOn] = DateTime.now()
                    it[version] = event.version
                }
                commit()
            } else {
                logger.error("account is created before")
            }
        }
    }

    fun project(event: DepositPerformed) {
        transaction {
            BankAccounts.select { aggregateID.eq(event.aggregateID) }
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
        }
    }

    fun project(event: OwnerChanged) {
        transaction {
            BankAccounts.select { aggregateID.eq(event.aggregateID) }
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
        }
    }

    fun project(event: WithdrawalPerformed): Option<Command> {
        transaction {
            BankAccounts.select { aggregateID.eq(event.aggregateID) }
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
        }

        return none()
    }
}