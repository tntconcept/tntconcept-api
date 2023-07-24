package com.autentia.tnt.binnacle.exception

internal class NoEvidenceInActivityException(val id: Long, message: String) : BinnacleException(message) {
    constructor(id: Long) : this(id, "Evidence in activity (id: $id) not found")
    constructor(message: String) : this(0, message)
}