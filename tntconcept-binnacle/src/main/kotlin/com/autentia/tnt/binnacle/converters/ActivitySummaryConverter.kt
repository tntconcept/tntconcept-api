package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.DailyWorkingTime
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import jakarta.inject.Singleton


@Singleton
class ActivitySummaryConverter {

    fun toActivitySummaryDTO(activitySummary: DailyWorkingTime) =
        ActivitySummaryDTO(
            activitySummary.date,
            activitySummary.workedHours,
        )

    fun toListActivitySummaryDTO(dailyWorkingTimes: List<DailyWorkingTime>) =
        dailyWorkingTimes.map { toActivitySummaryDTO(it) }.toList()

}