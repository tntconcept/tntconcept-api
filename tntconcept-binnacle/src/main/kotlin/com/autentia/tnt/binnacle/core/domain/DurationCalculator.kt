package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.Duration
import java.time.LocalDateTime

class DurationCalculator {
    companion object {
        fun getDuration(start: LocalDateTime, end: LocalDateTime, timeUnit: TimeUnit) =
            Duration.between(start, end).toMinutes().toInt()
    }
}