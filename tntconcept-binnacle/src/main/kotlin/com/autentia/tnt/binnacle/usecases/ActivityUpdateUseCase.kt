package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityTimeInterval
import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional

@Singleton
class ActivityUpdateUseCase internal constructor(
        private val activityRepository: ActivityRepository,
        private val activityCalendarService: ActivityCalendarService,
        private val projectRoleRepository: ProjectRoleRepository,
        private val userService: UserService,
        private val activityValidator: ActivityValidator,
        private val activityRequestBodyConverter: ActivityRequestBodyConverter,
        private val activityResponseConverter: ActivityResponseConverter,
        private val activityEvidenceService: ActivityEvidenceService,
        private val sendPendingApproveActivityMailUseCase: SendPendingApproveActivityMailUseCase
) {
    @Transactional
    @ReadOnly
    fun updateActivity(activityRequest: ActivityRequestDTO, locale: Locale): ActivityResponseDTO {
        val user = userService.getAuthenticatedDomainUser()
        val projectRoleEntity = this.getProjectRoleEntity(activityRequest.projectRoleId)
        val projectRole = projectRoleEntity.toDomain()
        val currentActivity = this.getActivity(activityRequest.id!!)

        val duration = activityCalendarService.getDurationByCountingWorkingDays(
                ActivityTimeInterval.of(activityRequest.interval.toDomain(), projectRole.getTimeUnit()))

        val activityToUpdate = activityRequestBodyConverter.toActivity(
                activityRequest,
                duration,
                currentActivity.insertDate,
                projectRole,
                user
        )

        activityValidator.checkActivityIsValidForUpdate(activityToUpdate, currentActivity, user)

        val updatedActivityEntity = activityRepository.update(Activity.of(activityToUpdate, projectRoleEntity))

        val updatedActivity = updatedActivityEntity.toDomain().copy(evidence = activityToUpdate.evidence);

        if (activityToUpdate.hasEvidences) {
            activityEvidenceService.storeActivityEvidence(
                    updatedActivityEntity.id!!,
                    activityToUpdate.evidence!!,
                    updatedActivityEntity.insertDate!!
            )
        }

        if (!activityToUpdate.hasEvidences && currentActivity.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(
                    updatedActivityEntity.id!!,
                    updatedActivityEntity.insertDate!!
            )
        }

        sendActivityPendingOfApprovalEmailIfNeeded(projectRole, currentActivity, updatedActivity, user, locale)

        return activityResponseConverter.toActivityResponseDTO(updatedActivity)
    }

    private fun sendActivityPendingOfApprovalEmailIfNeeded(
        projectRole: ProjectRole,
        originalActivity: com.autentia.tnt.binnacle.core.domain.Activity,
        updatedActivity: com.autentia.tnt.binnacle.core.domain.Activity,
        user: User,
        locale: Locale
    ) {
        val attachedEvidenceHasChanged = updatedActivity.evidence !== null && (originalActivity.evidence === null || originalActivity.evidence != updatedActivity.evidence)
        val projectRoleHasChanged = originalActivity.projectRole != updatedActivity.projectRole

        if (projectRole.requireEvidence() && updatedActivity.canBeApproved() && (attachedEvidenceHasChanged || projectRoleHasChanged)
        ) {
            sendPendingApproveActivityMailUseCase.send(updatedActivity, user.username, locale)
        } else if (!projectRole.requireEvidence() && updatedActivity.canBeApproved() && projectRoleHasChanged) {
            sendPendingApproveActivityMailUseCase.send(updatedActivity, user.username, locale)
        }
    }

    private fun getProjectRoleEntity(projectRoleId: Long) =
            projectRoleRepository.findById(projectRoleId) ?: throw ProjectRoleNotFoundException(projectRoleId)

    private fun getActivity(activityId: Long): com.autentia.tnt.binnacle.core.domain.Activity {
        val activity = Optional.ofNullable(activityRepository.findById(activityId))
            .orElseThrow { ActivityNotFoundException(activityId) }.toDomain()
        if (activity.hasEvidences) {
            val evidence =
                activityEvidenceService.getActivityEvidence(activityId, toDate(activity.insertDate)!!).toDomain()
            return activity.copy(evidence = evidence)
        }
        return activity
    }
}