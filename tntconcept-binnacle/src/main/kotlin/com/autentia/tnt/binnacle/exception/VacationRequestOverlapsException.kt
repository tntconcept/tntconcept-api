package com.autentia.tnt.binnacle.exception

class VacationRequestOverlapsException(message: String) : BinnacleException(message) {
    constructor() : this("Some days overlap with previous vacation request")
}
