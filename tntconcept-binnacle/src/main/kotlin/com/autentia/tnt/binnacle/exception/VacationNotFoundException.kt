package com.autentia.tnt.binnacle.exception

class VacationNotFoundException(val id: Long, message: String) : ResourceNotFoundException(message) {
    constructor(id: Long) : this(id, "Vacation (id: $id) not found")
}
