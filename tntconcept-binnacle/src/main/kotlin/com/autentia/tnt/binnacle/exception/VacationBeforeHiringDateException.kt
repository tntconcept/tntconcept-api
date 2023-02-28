package com.autentia.tnt.binnacle.exception

class VacationBeforeHiringDateException(message: String) : BinnacleException(message) {
    constructor() : this("The vacation start date is before user hiring date")
}
