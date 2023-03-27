package com.autentia.tnt.binnacle.exception

import java.time.LocalDateTime

class TimeIntervalException(message: String) : BinnacleException(message) {
    constructor(start: LocalDateTime, end: LocalDateTime) :
            this("End date must be equal or later than start date: (startDate: $start, endDate: $end)")
}