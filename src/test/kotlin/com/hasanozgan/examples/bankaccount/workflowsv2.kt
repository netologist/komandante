package com.hasanozgan.examples.bankaccount

import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.Workflow

class BankAccountWorkflowV2 : Workflow<BankAccountEvent>() {
    fun run(event: AccountCreated): List<Command> {
        return listOf(SendMessage("account created ${event.owner} for ${event.aggregateID}"))
    }

    fun run(event: DepositPerformed): List<Command> {
        return listOf(SendMessage("${event.amount} deposit performed for ${event.aggregateID}"))
    }

    fun run(event: OwnerChanged): List<Command> {
        return listOf(SendMessage("${event.owner} owner changed for ${event.aggregateID}"))
    }

    fun run(event: WithdrawalPerformed): List<Command> {
        return listOf(SendMessage("${event.amount} withdrawal performed for ${event.aggregateID}"))
    }
}