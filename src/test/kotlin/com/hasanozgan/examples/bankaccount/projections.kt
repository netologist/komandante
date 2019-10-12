package com.hasanozgan.examples.bankaccount

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import java.util.*

object BankAccounts : UUIDTable() {
    var aggregateID = uuid("aggregate_id")
    var owner = varchar("owner", 255)
    var balance = double("balance")
    var updatedOn = datetime("updated_on")
    var version = integer("version")
}

class BankAccount(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BankAccount>(BankAccounts)

    var aggregateID = BankAccounts.aggregateID
    var owner = BankAccounts.owner
    var balance = BankAccounts.balance
    var updatedOn = BankAccounts.updatedOn
    var version = BankAccounts.version
}