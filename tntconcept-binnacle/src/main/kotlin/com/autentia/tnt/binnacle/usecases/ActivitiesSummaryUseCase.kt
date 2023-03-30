package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.ActivitySummaryConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ActivitiesSummaryUseCase internal constructor(
    private val activityCalendarService: ActivityCalendarService,
    private val activityService: ActivityService,
    private val activitiesSummaryConverter: ActivitySummaryConverter,
    private val activityResponseConverter: ActivityResponseConverter,
    private val userService: UserService,
) {

    fun getActivitiesSummary(startDate: LocalDate, endDate: LocalDate): List<ActivitySummaryDTO> {
        val user = userService.getAuthenticatedUser()
        val dateInterval = DateInterval.of(startDate, endDate)
        val activities =
            activityService.getActivitiesBetweenDates(dateInterval, user.id).map(activityResponseConverter::toActivity)
        val activityDurationSummaryInHours =
            activityCalendarService.getActivityDurationSummaryInHours(activities, dateInterval)
        return activitiesSummaryConverter.toListActivitySummaryDTO(activityDurationSummaryInHours)
    }
}