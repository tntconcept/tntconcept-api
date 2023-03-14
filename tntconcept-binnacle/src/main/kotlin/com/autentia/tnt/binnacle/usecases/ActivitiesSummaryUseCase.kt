package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivitySummaryConverter
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ActivitiesSummaryUseCase internal constructor(
    private val activityService: ActivityService,
    private val userService: UserService,
    private val activitySummaryConverter: ActivitySummaryConverter
) {
    fun getActivitiesSummary(start: LocalDate, end: LocalDate): List<ActivitySummaryDTO> {
        val user = userService.getAuthenticatedUser()
        val activitiesResponse = activityService.getActivitiesBetweenDates(start, end, user.id)
        val listActivitySummary = activitySummaryConverter.toListActivitySummaryDate(activitiesResponse, start, end)
        return listActivitySummary.map { activitySummaryConverter.toActivitySummaryDTO(it) }
    }
}