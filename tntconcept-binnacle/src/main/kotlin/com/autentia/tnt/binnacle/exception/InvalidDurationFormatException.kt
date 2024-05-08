package com.autentia.tnt.binnacle.exception

class InvalidDurationFormatException (message: String) : BinnacleException(message) {
    constructor() : this("The duration must be greater than 0")
}