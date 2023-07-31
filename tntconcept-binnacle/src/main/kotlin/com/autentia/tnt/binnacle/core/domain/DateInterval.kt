package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth

data class DateInterval private constructor(val start: LocalDate, val end: LocalDate) {

    companion object {
        fun of(start: LocalDate, end: LocalDate) = DateInterval(start, end)
        fun of(year: Int, month: Month): DateInterval {
            val yearMonth = YearMonth.of(year, month)
            return of(yearMonth.atDay(1), yearMonth.atEndOfMonth())
        }

        fun ofYear(year: Int) = of(
            LocalDate.of(year, Month.JANUARY, 1),
            LocalDate.of(year, Month.DECEMBER, 31)
        )

        fun getDateIntervalForActivityList (
            activities: List<Activity>,
            defaultTimeInterval: TimeInterval,
        ): DateInterval {

            if (activities.isNotEmpty()) {
                val minDate = activities.minOf { it.getStart() }
                val maxDate = activities.maxOf { it.getEnd() }

                return DateInterval.of(minDate.toLocalDate(), maxDate.toLocalDate())
            }

            return defaultTimeInterval.getDateInterval()

        }

    }

    fun includes(localDate: LocalDate) = !localDate.isBefore(start) && !localDate.isAfter(end)
}