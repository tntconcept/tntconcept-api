package com.autentia.tnt.binnacle.exception

class NoEvidenceInActivityException(val id: Long, message: String) : BinnacleException(message) {
    constructor(id: Long) : this(id, "Activity (id: $id) not found")
}
