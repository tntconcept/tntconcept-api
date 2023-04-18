package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.TimeIntervalConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import java.util.*
import javax.validation.Valid

@Singleton
@Validated
class ActivityCreationUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleService: ProjectRoleService,
    private val userService: UserService,
    private val activityValidator: ActivityValidator,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter,
    private val timeIntervalConverter: TimeIntervalConverter,
    private val approveActivityMailService: ApproveActivityMailService,
) {

    fun createActivity(@Valid activityRequestBody: ActivityRequestBodyDTO, locale: Locale): ActivityResponseDTO {
        val user = userService.getAuthenticatedUser()
        val projectRole = projectRoleService.getByProjectRoleId(activityRequestBody.projectRoleId)
        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            timeIntervalConverter.toTimeInterval(activityRequestBody.interval), projectRole.timeUnit
        )

        val activityRequest = activityRequestBodyConverter
            .mapActivityRequestBodyDTOToActivityRequestBody(activityRequestBody, projectRole, duration)

        activityValidator.checkActivityIsValidForCreation(activityRequest, user)
        val activityResponse = activityResponseConverter.mapActivityToActivityResponse(
            activityService.createActivity(
                activityRequest, user
            )
        )

        if (activityResponse.projectRole.isApprovalRequired){
            approveActivityMailService.sendApprovalActivityMail(activityResponse, user.email, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(activityResponse)
    }
}