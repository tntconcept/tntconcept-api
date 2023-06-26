package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivitiesResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityDateConverter
import com.autentia.tnt.binnacle.core.domain.ActivitiesResponse
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.UserService
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime

@Singleton
class ActivitiesBetweenDateHookUseCase internal constructor(
    private val userService: UserService,
    private val activityDateConverter: ActivityDateConverter,
    private val activityResponseConverter: ActivitiesResponseConverter,
    @param:Named("Internal") private val activityRepository: ActivityRepository,
) {
    fun getActivities(startDate: LocalDate, endDate: LocalDate, userName: String): List<ActivityDateDTO> {
        val user = userService.getByUserName(userName)
        val activitiesResponse = getUserActivitiesBetweenDates(startDate, endDate, user.id)
        val listActivityDate = activityDateConverter.toListActivityDate(activitiesResponse, startDate, endDate)
        return listActivityDate.map { activityDateConverter.toActivityDateDTO(it) }
    }

    fun getUserActivitiesBetweenDates(
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Long
    ): List<ActivitiesResponse> {
        val startDateMinHour = startDate.atTime(LocalTime.MIN)
        val endDateMaxHour = endDate.atTime(23, 59, 59)
        return activityRepository
            .findByUserId(startDateMinHour, endDateMaxHour, userId)
            .map { activityResponseConverter.mapActivityToActivityResponse(it) }
    }
}