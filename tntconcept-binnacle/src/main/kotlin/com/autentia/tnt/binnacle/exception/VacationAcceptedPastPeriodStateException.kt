package com.autentia.tnt.binnacle.exception

class VacationAcceptedPastPeriodStateException(message: String) : BinnacleException(message) {
    constructor() :
            this("The accepted vacation period is previous to the current date")
}
