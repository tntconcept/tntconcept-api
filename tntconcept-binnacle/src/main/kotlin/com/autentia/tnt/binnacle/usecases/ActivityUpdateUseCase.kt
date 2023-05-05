package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.converters.TimeIntervalConverter
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.validators.ActivityValidator
import jakarta.inject.Singleton
import java.util.*

@Singleton
class ActivityUpdateUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleService: ProjectRoleService,
    private val userService: UserService,
    private val activityValidator: ActivityValidator,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter,
    private val timeIntervalConverter: TimeIntervalConverter,
    private val activityPendingApprovalMailService: ActivityPendingApprovalMailService,
) {
    fun updateActivity(activityRequest: ActivityRequestBodyDTO, locale: Locale): ActivityResponseDTO {
        val user = userService.getAuthenticatedUser()
        val projectRole = projectRoleService.getByProjectRoleId(activityRequest.projectRoleId)
        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            timeIntervalConverter.toTimeInterval(activityRequest.interval), projectRole.timeUnit
        )

        val activityRequestBody =
            activityRequestBodyConverter.mapActivityRequestBodyDTOToActivityRequestBody(
                activityRequest, projectRole, duration
            )

        activityValidator.checkActivityIsValidForUpdate(activityRequestBody, user)

        val activityResponse = activityResponseConverter.mapActivityToActivityResponse(
            activityService.updateActivity(activityRequestBody, user)
        )

        if (activityCanBeApproved(
                activityResponse.projectRole.requireEvidence,
                activityResponse.approvalState,
                activityResponse.hasEvidences
            )
        ) {
            activityPendingApprovalMailService.sendActivityEvidenceMail(activityResponse, user.username, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(activityResponse)
    }

    fun activityCanBeApproved(requireEvidence: RequireEvidence, approvalState: ApprovalState, hasEvidences: Boolean?) =
        RequireEvidence.isRequired(requireEvidence) &&
                approvalState === ApprovalState.PENDING &&
                hasEvidences == true
}