package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.LocalDateTime
import javax.persistence.Embeddable

@Embeddable
data class Interval(
    val start: LocalDateTime,
    val end: LocalDateTime
) {
    fun getDuration(timeUnit: TimeUnit) = DurationCalculator.getDuration(start, end, timeUnit)
}