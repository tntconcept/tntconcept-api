package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityDateConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.services.ActivitiesService
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class ActivitiesBetweenDateHookUseCase internal constructor(
    private val activityService: ActivitiesService,
    private val userService: UserService,
    private val activityDateConverter: ActivityDateConverter
) {
    fun getActivities(startDate: LocalDate, endDate: LocalDate, userName: String): List<ActivityDateDTO> {
        val user = userService.getByUserName(userName)
        val activitiesResponse = activityService.getUserActivitiesBetweenDates(startDate, endDate, user.id)
        val listActivityDate = activityDateConverter.toListActivityDate(activitiesResponse, startDate, endDate)
        return listActivityDate.map { activityDateConverter.toActivityDateDTO(it) }
    }
}