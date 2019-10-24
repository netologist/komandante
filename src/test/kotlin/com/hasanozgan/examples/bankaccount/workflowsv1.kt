package com.hasanozgan.examples.bankaccount

import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.Workflow

class BankAccountWorkflowV1 : Workflow<BankAccountEvent>() {
    override fun run(event: Event): List<Command> {
        when (event) {
            is AccountCreated ->
                return listOf(SendMessage("account created ${event.owner} for ${event.aggregateID}"))

            is DepositPerformed ->
                return listOf(SendMessage("${event.amount} deposit performed for ${event.aggregateID}"))

            is OwnerChanged ->
                return listOf(SendMessage("${event.owner} owner changed for ${event.aggregateID}"))

            is WithdrawalPerformed ->
                return listOf(SendMessage("${event.amount} withdrawal performed for ${event.aggregateID}"))
        }
        return emptyList()
    }
}