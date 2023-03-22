package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.TimeIntervalConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class ActivityCreationUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val userService: UserService,
    private val activityValidator: ActivityValidator,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter,
    private val timeIntervalConverter: TimeIntervalConverter
) {

    fun createActivity(@Valid activityRequestBody: ActivityRequestBodyDTO): ActivityResponseDTO {
        val user = userService.getAuthenticatedUser()

        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            timeIntervalConverter.toTimeInterval(activityRequestBody.interval), activityRequestBody.projectRoleId
        )

        val activityRequest = activityRequestBodyConverter
            .mapActivityRequestBodyDTOToActivityRequestBody(activityRequestBody, duration)

        activityValidator.checkActivityIsValidForCreation(activityRequest, user)
        val activityResponse = activityResponseConverter.mapActivityToActivityResponse(
            activityService.createActivity(
                activityRequest,
                user
            )
        )

        return activityResponseConverter.toActivityResponseDTO(activityResponse)
    }
}