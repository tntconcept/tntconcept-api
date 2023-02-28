package com.autentia.tnt.binnacle.exception

import java.time.LocalDate

class DateRangeException(message: String) : BinnacleException(message) {
    constructor(startDate: LocalDate, endDate: LocalDate) :
            this("End date must be equal or later than start date: (startDate: $startDate, endDate: $endDate)")
}
