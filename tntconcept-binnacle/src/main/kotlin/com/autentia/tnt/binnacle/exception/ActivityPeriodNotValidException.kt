package com.autentia.tnt.binnacle.exception

class ActivityPeriodNotValidException(message: String) : BinnacleException(message) {
    constructor() : this("The period of time of the activity is longer than a day for the given project role.")
}
