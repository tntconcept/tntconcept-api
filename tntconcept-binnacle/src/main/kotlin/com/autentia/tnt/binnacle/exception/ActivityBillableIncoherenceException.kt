package com.autentia.tnt.binnacle.exception

class ActivityBillableIncoherenceException(message: String) : BinnacleException(message) {
    constructor() : this("The billing field of the activity is incoherent with the project billing type")
}