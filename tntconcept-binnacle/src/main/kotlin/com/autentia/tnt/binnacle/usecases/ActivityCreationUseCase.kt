package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityTimeInterval
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ApproveActivityMailService
import com.autentia.tnt.binnacle.services.ProjectRoleService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import java.util.Locale
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
    private val approveActivityMailService: ApproveActivityMailService,
) {

    fun createActivity(@Valid activityRequestBody: ActivityRequestBodyDTO, locale: Locale): ActivityResponseDTO {
        val user = userService.getAuthenticatedDomainUser()
        val projectRole = projectRoleService.getByProjectRoleId(activityRequestBody.projectRoleId)
        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            ActivityTimeInterval.of(activityRequestBody.interval.toDomain(), projectRole.timeUnit)
        )

        val activityToCreate = activityRequestBodyConverter.toActivity(activityRequestBody, duration, projectRole, user)

        activityValidator.checkActivityIsValidForCreation(activityToCreate, user)
        val activityCreated = activityService.createActivity(activityToCreate, activityRequestBody.imageFile)

        if (activityCreated.projectRole.isApprovalRequired) {
            approveActivityMailService.sendApprovalActivityMail(activityCreated, user.username, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(activityCreated)
    }
}