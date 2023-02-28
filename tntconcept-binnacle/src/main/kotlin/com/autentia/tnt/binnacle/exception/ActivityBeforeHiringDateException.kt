package com.autentia.tnt.binnacle.exception

class ActivityBeforeHiringDateException(message: String) : BinnacleException(message) {
    constructor() : this("The activity start date is before user hiring date")
}
