package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityTimeInterval
import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import com.autentia.tnt.security.application.checkSubcontractedActivityManagerRole
import io.micronaut.security.utils.SecurityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional

@Singleton
class SubcontractedActivityUpdateUseCase internal constructor(
        private val activityRepository: ActivityRepository,
        private val projectRoleRepository: ProjectRoleRepository,
        private val userService: UserService,
        private val subcontractedActivityValidator: SubcontractedActivityValidator,
        private val activityRequestBodyConverter: ActivityRequestBodyConverter,
        private val activityResponseConverter: ActivityResponseConverter,
        private val activityEvidenceService: ActivityEvidenceService,
        private val securityService: SecurityService,
        private val sendPendingApproveActivityMailUseCase: SendPendingApproveActivityMailUseCase
){
    @Transactional
    @ReadOnly
    fun updateSubcontractedActivity(subcontractedActivityRequest: SubcontractedActivityRequestDTO, locale: Locale): SubcontractedActivityResponseDTO {
        securityService.checkSubcontractedActivityManagerRole()
        val user = userService.getAuthenticatedDomainUser()
        val projectRoleEntity = this.getProjectRoleEntity(subcontractedActivityRequest.projectRoleId)
        val projectRole = projectRoleEntity.toDomain()
        val currentActivity = this.getActivity(subcontractedActivityRequest.id!!)


        val activityToUpdate = activityRequestBodyConverter.toActivity(
                subcontractedActivityRequest,
                currentActivity.insertDate,
                projectRole,
                user
        )

        subcontractedActivityValidator.checkActivityIsValidForUpdate(activityToUpdate, currentActivity, user)

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

//        sendActivityPendingOfApprovalEmailIfNeeded(projectRole, currentActivity, updatedActivity, user, locale)

        return activityResponseConverter.toSubcontractingActivityResponseDTO(updatedActivity)
    }

//    private fun sendActivityPendingOfApprovalEmailIfNeeded(
//            projectRole: ProjectRole,
//            originalActivity: com.autentia.tnt.binnacle.core.domain.Activity,
//            updatedActivity: com.autentia.tnt.binnacle.core.domain.Activity,
//            user: User,
//            locale: Locale
//    ) {
//        val attachedEvidenceHasChanged = updatedActivity.evidence !== null && (originalActivity.evidence === null || originalActivity.evidence != updatedActivity.evidence)
//        val projectRoleHasChanged = originalActivity.projectRole != updatedActivity.projectRole
//
//        val projectRequiresEvidenceAndActivityCanBeApproved =
//                projectRole.requireEvidence() && updatedActivity.canBeApproved() && (attachedEvidenceHasChanged || projectRoleHasChanged)
//        val projectDoesNotRequireEvidenceAndActivityCanBeApproved = !projectRole.requireEvidence() && updatedActivity.canBeApproved() && projectRoleHasChanged
//
//        if (projectRequiresEvidenceAndActivityCanBeApproved || projectDoesNotRequireEvidenceAndActivityCanBeApproved){
//            sendPendingApproveActivityMailUseCase.send(updatedActivity, user.username, locale)
//        }
//    }

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