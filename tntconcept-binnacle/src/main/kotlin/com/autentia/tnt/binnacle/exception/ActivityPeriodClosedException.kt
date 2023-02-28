package com.autentia.tnt.binnacle.exception

class ActivityPeriodClosedException(message: String) : BinnacleException(message) {
    constructor() : this("The period of time of the activity is closed for modifications")
}
