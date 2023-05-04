package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityDateConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.services.ActivitiesService
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton
import java.time.LocalDate

@Deprecated("Use ActivitiesByFilterUseCase instead")
@Singleton
class ActivitiesBetweenDateUseCaseOld internal constructor(
    private val activityService: ActivitiesService,
    private val userService: UserService,
    private val activityDateConverter: ActivityDateConverter
) {

    fun getActivities(startDate: LocalDate, endDate: LocalDate): List<ActivityDateDTO> {
        val user = userService.getAuthenticatedUser()
        val activitiesResponse = activityService.getActivitiesBetweenDates(startDate, endDate, user.id)
        val listActivityDate = activityDateConverter.toListActivityDate(activitiesResponse, startDate, endDate)
        return listActivityDate.map { activityDateConverter.toActivityDateDTO(it) }
    }

}