package com.autentia.tnt.binnacle.exception

class ActivityNotFoundException(val id: Long, message: String) : ResourceNotFoundException(message) {
    constructor(id: Long) : this(id, "Activity (id: $id) not found")
}
