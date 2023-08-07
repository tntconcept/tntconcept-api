package com.autentia.tnt.binnacle.exception

class EvidenceNotFoundException(message: String) : BinnacleException(message) {

    constructor() : this("Evidence attachment does not exist")
}