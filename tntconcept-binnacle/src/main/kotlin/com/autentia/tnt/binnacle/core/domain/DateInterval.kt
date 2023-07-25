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

        fun getDateIntervalForRemainingCalculation(
            timeInterval: TimeInterval,
            activities: List<Activity>,
        ): DateInterval {

            var endYearOrLastActivityDate = timeInterval.end

            if (activities.isNotEmpty()) {
                endYearOrLastActivityDate = activities.maxOf { it.getEnd() }
            }

            val startDate = timeInterval.start

            var dateIntervalToGetRemaining = timeInterval.getDateInterval()

            if (endYearOrLastActivityDate.year > timeInterval.end.year)
                dateIntervalToGetRemaining = TimeInterval.of(
                    LocalDate.of(
                        startDate.year,
                        startDate.month,
                        startDate.dayOfMonth,
                    ).atTime(LocalTime.MIN),
                    LocalDate.of(
                        endYearOrLastActivityDate.year,
                        endYearOrLastActivityDate.month,
                        endYearOrLastActivityDate.dayOfMonth
                    ).atTime(LocalTime.MAX)
                ).getDateInterval()

            return dateIntervalToGetRemaining
        }
    }

    fun includes(localDate: LocalDate) = !localDate.isBefore(start) && !localDate.isAfter(end)
}