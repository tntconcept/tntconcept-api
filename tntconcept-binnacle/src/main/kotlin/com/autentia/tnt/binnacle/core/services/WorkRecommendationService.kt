package com.autentia.tnt.binnacle.core.services

import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import kotlin.time.Duration

interface WorkRecommendationService {
    fun suggestWorkingTimeByMonth(
        currentYearMonth: YearMonth,
        hiringDate: LocalDate,
        targetTime: Duration,
        workedTime: Map<Month, Duration>,
        workableTime: Map<Month, Duration>,
    ): Map<Month, Duration>
}
