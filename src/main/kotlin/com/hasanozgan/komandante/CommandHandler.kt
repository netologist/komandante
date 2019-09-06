package com.hasanozgan.komandante

interface CommandHandler {
    fun handle(command: Command): Result<Event, DomainError>
}
