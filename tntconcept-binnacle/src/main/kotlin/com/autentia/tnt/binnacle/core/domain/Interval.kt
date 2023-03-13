package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.IntervalRequestDTO
import java.time.LocalDateTime
import javax.persistence.Embeddable

@Embeddable
data class Interval(
    val start: LocalDateTime,
    val end: LocalDateTime
) {
    constructor(interval: IntervalRequestDTO) : this(interval.start, interval.end)
    
    fun getDuration(timeUnit: TimeUnit) = DurationCalculator.getDuration(start, end, timeUnit)
}