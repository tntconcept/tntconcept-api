package com.autentia.tnt.binnacle.exception

class NoMoreDaysLeftInYearException(message: String) : BinnacleException(message) {
    constructor() : this("There are no more days left in selected year")
}