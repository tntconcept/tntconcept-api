package com.autentia.tnt.binnacle.exception

class InvalidDurationFormatException (message: String) : BinnacleException(message) {
    constructor() : this("Invalid duration format")
}