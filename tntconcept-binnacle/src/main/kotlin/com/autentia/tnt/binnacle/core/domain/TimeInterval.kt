package com.autentia.tnt.binnacle.core.domain

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
    fun getYearOfStart() = start.year
    fun getDuration(): Duration = Duration.between(start, end)
    fun getDurationInMinutes() = getDuration().toMinutes()
    fun getDurationInDays() = getDuration().toDays().toInt() + 1

    fun isOneDay() = start.toLocalDate().isEqual(end.toLocalDate())

    fun isInTheTimeInterval(timeInterval: TimeInterval) =
        (start.isBefore(timeInterval.end) || start.isEqual(timeInterval.end))
                && (end.isAfter(timeInterval.start) || end.isEqual(timeInterval.start))
}