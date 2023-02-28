package com.autentia.tnt.binnacle.exception

class VacationRequestEmptyException(message: String) : BinnacleException(message) {
    constructor() : this("You must request at least one vacation day")
}
