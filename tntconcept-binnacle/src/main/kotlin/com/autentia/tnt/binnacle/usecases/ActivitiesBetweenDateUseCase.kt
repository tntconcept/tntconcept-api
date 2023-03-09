package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityDateConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ActivitiesBetweenDateUseCase internal constructor(
    private val activityService: ActivityService,
    private val userService: UserService,
    private val activityDateConverter: ActivityDateConverter
) {

    fun getActivities(start: LocalDate, end: LocalDate): List<ActivityDateDTO> {
        val user = userService.getAuthenticatedUser()
        val activitiesResponse = activityService.getActivitiesBetweenDates(start, end, user.id)
        val listActivityDate = activityDateConverter.toListActivityDate(activitiesResponse, start, end)
        return listActivityDate.map { activityDateConverter.toActivityDateDTO(it) }
    }

}
