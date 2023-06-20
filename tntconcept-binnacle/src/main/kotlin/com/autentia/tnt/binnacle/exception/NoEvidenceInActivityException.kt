package com.autentia.tnt.binnacle.exception

class NoEvidenceInActivityException(message: String) : BinnacleException(message) {
    constructor(id: Long) : this("Evidence in activity (id: $id) not found")
    constructor(id: Long, message: String) : this("Activity (id: $id) sets hasEvidence to true but no evidence was found")
}