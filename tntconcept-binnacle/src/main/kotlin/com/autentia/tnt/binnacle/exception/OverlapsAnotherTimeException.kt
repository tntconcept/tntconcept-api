package com.autentia.tnt.binnacle.exception

class OverlapsAnotherTimeException(message: String) : BinnacleException(message) {
    constructor() : this("There is already an activity in the indicated period of time")
}
