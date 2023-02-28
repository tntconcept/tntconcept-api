package com.autentia.tnt.binnacle.core.services

import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class WorkRecommendationCurrentMonthAccumulationService : WorkRecommendationService {
    override fun suggestWorkingTimeByMonth(
        currentYearMonth: YearMonth,
        hiringDate: LocalDate,
        targetTime: Duration,
        workedTime: Map<Month, Duration>,
        workableTime: Map<Month, Duration>,
    ): Map<Month, Duration> {
        var suggestFrom: YearMonth =
            when {
                hiringDate.year < currentYearMonth.year -> YearMonth.of(currentYearMonth.year, Month.JANUARY)
                hiringDate.year == currentYearMonth.year -> YearMonth.of(currentYearMonth.year, hiringDate.month)
                else -> return emptyMap()
            }
        val mapSuggestWorkingTimeEachMonth = mutableMapOf<Month, Duration>()
        var totalWorkedTime: Duration = Duration.ZERO
        var prorateMaxAccumulatedTime: Duration = Duration.ZERO
        val annualWorkableTotalTime = workableTime.values.fold(Duration.ZERO, Duration::plus)
        while (suggestFrom <= YearMonth.of(currentYearMonth.year, Month.DECEMBER)) {

            val prorate = prorateOfMonthlyMaxHours(
                targetTime,
                annualWorkableTotalTime,
                workableTime.getOrDefault(suggestFrom.month, Duration.ZERO)
            )

            mapSuggestWorkingTimeEachMonth[suggestFrom.month] = prorate

            if (suggestFrom <= currentYearMonth) {

                mapSuggestWorkingTimeEachMonth[suggestFrom.month] = suggestWorkingTime(
                    prorate,
                    totalWorkedTime,
                    prorateMaxAccumulatedTime
                )

                prorateMaxAccumulatedTime = prorateMaxAccumulatedTime.plus(prorate)
                totalWorkedTime = totalWorkedTime.plus(workedTime.getOrDefault(suggestFrom.month, Duration.ZERO))

            }

            suggestFrom = suggestFrom.plusMonths(1)
        }

        return mapSuggestWorkingTimeEachMonth
    }

    private fun suggestWorkingTime(
        prorate: Duration,
        totalWorkedTime: Duration,
        prorateMaxAccumulatedTime: Duration,
    ): Duration {
        val prorateAccumulatedBalance = totalWorkedTime.minus(prorateMaxAccumulatedTime)
        return prorate.minus(prorateAccumulatedBalance)
    }

    private fun prorateOfMonthlyMaxHours(
        targetTime: Duration,
        annualWorkableTime: Duration,
        workableTimeOfMonth: Duration,
    ): Duration {
        return (workableTimeOfMonth.inWholeSeconds * targetTime.inWholeSeconds / annualWorkableTime.inWholeSeconds).toDuration(
            DurationUnit.SECONDS
        )
    }
}
