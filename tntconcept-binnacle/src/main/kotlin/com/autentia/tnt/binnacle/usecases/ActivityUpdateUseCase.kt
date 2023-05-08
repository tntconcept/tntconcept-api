package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityTimeInterval
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityEvidenceMailService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ProjectRoleService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import jakarta.inject.Singleton
import java.util.Locale

@Singleton
class ActivityUpdateUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleService: ProjectRoleService,
    private val userService: UserService,
    private val activityValidator: ActivityValidator,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter,
    private val activityEvidenceMailService: ActivityEvidenceMailService
) {
    fun updateActivity(activityRequest: ActivityRequestBodyDTO, locale: Locale): ActivityResponseDTO {

        val user = userService.getAuthenticatedDomainUser()
        val projectRole = projectRoleService.getByProjectRoleId(activityRequest.projectRoleId)
        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            ActivityTimeInterval.of(activityRequest.interval.toDomain(), projectRole.timeUnit)
        )

        val currentActivity = activityService.getActivityById(activityRequest.id!!)

        val activityToUpdate = activityRequestBodyConverter.toActivity(
            activityRequest,
            duration,
            currentActivity.insertDate,
            projectRole,
            user
        )

        activityValidator.checkActivityIsValidForUpdate(activityToUpdate, currentActivity, user)
        val updatedActivity = activityService.updateActivity(activityToUpdate, activityRequest.imageFile)


        if (updatedActivity.activityCanBeApproved()) {
            activityEvidenceMailService.sendActivityEvidenceMail(updatedActivity, user.username, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(updatedActivity)
    }
}