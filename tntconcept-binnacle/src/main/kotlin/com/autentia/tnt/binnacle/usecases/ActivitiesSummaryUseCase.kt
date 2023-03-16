package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivitySummaryConverter
import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ActivitiesSummaryUseCase internal constructor(
    private val activityCalendarService: ActivityCalendarService,
    private val activitiesSummaryConverter: ActivitySummaryConverter,
    private val userService: UserService,
) {

    fun getActivitiesSummary(startDate: LocalDate, endDate: LocalDate): List<ActivitySummaryDTO> {
        val user = userService.getAuthenticatedUser()
        val interval = DateInterval.of(startDate, endDate)
        val activityDurationSummaryInHours = activityCalendarService.getActivityDurationSummaryInHours(interval, user.id)
        return activitiesSummaryConverter.toListActivitySummaryDTO(activityDurationSummaryInHours)
    }
}