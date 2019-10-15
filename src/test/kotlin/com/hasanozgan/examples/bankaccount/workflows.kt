package com.hasanozgan.examples.bankaccount

import com.hasanozgan.komandante.Command
import com.hasanozgan.komandante.Event
import com.hasanozgan.komandante.Workflow

class BankAccountWorkflow : Workflow<BankAccountEvent> {
    override fun <T : Event> run(event: T): List<Command> {

        println(event)

        when (event) {
            is AccountCreated ->
                return emptyList()

            is DepositPerformed ->
                return emptyList()

            is OwnerChanged ->
                return emptyList()

            is WithdrawalPerformed ->
                return emptyList()
        }
        return emptyList()
    }
}