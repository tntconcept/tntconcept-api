package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivitySummaryConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime

@Singleton
class ActivitiesSummaryUseCase internal constructor(
    private val activityCalendarService: ActivityCalendarService,
    private val activityRepository: ActivityRepository,
    private val activitiesSummaryConverter: ActivitySummaryConverter,
    private val securityService: SecurityService
) {
    fun getActivitiesSummary(startDate: LocalDate, endDate: LocalDate): List<ActivitySummaryDTO> {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()
        val dateInterval = DateInterval.of(startDate, endDate)
        val startDateMinHour = dateInterval.start.atTime(LocalTime.MIN)
        val endDateMaxHour = dateInterval.end.atTime(LocalTime.MAX)

        val activities =
            activityRepository.findByUserId(startDateMinHour, endDateMaxHour, userId).map(Activity::toDomain)
        val activityDurationSummaryInHours =
            activityCalendarService.getActivityDurationSummaryInHours(activities, dateInterval)
        return activitiesSummaryConverter.toListActivitySummaryDTO(activityDurationSummaryInHours)
    }
}