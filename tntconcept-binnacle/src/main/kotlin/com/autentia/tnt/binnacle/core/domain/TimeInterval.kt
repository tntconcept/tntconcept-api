package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.exception.TimeIntervalException
import java.time.Duration
import java.time.LocalDateTime
import java.time.Month

data class TimeInterval private constructor(val start: LocalDateTime, val end: LocalDateTime) {

    companion object {
        fun of(start: LocalDateTime, end: LocalDateTime): TimeInterval {
            if (start.isAfter(end)) throw TimeIntervalException(end, start)
            return TimeInterval(start, end)
        }

        fun ofYear(year: Int) = of(
            LocalDateTime.of(year, Month.JANUARY, 1, 0, 0),
            LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59)
        )
    }

    fun getDateInterval() = DateInterval.of(start.toLocalDate(), end.toLocalDate())

    fun getDuration(): Duration = Duration.between(start, end)
}