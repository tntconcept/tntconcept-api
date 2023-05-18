package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivitySummaryConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ActivitiesSummaryUseCase internal constructor(
    private val activityCalendarService: ActivityCalendarService,
    private val activityService: ActivityService,
    private val activitiesSummaryConverter: ActivitySummaryConverter,
) {

    fun getActivitiesSummary(startDate: LocalDate, endDate: LocalDate): List<ActivitySummaryDTO> {
        val dateInterval = DateInterval.of(startDate, endDate)
        val activities =
            activityService.getActivitiesBetweenDates(dateInterval).map(Activity::toDomain)
        val activityDurationSummaryInHours =
            activityCalendarService.getActivityDurationSummaryInHours(activities, dateInterval)
        return activitiesSummaryConverter.toListActivitySummaryDTO(activityDurationSummaryInHours)
    }
}