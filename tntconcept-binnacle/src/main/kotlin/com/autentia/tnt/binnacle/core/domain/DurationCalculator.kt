package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.Duration
import java.time.LocalDateTime

class DurationCalculator {
    companion object {
        fun getDuration(start: LocalDateTime, end: LocalDateTime, timeUnit: TimeUnit) =
            if (timeUnit == TimeUnit.MINUTES) {
                Duration.between(start, end).toMinutes().toInt()
            } else {
                Duration.between(start, end).toDays().toInt() * 8 * 60
            }
    }
}