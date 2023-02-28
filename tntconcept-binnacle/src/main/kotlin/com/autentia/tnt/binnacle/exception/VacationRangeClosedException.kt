package com.autentia.tnt.binnacle.exception

class VacationRangeClosedException(message: String) : BinnacleException(message) {
    constructor() : this("Vacation range is closed for modifications")
}
