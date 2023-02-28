package com.autentia.tnt.binnacle.exception

class VacationAcceptedStateException(message: String) : BinnacleException(message) {
    constructor() :
            this("The vacation period is accepted")
}
