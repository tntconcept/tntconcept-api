package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ActivitiesBetweenDateUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityResponseConverter: ActivityResponseConverter
) {

    fun getActivities(
        start: LocalDate, end: LocalDate
    ): List<ActivityResponseDTO> {
        val activities = activityService.getActivitiesBetweenDates(DateInterval.of(start, end))
        return activityResponseConverter.mapActivitiesToActivitiesResponseDTO(activities)
    }
}
