package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
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
    private val userRepository: UserRepository,
    private val subcontractedActivityValidator: SubcontractedActivityValidator,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter,
    private val activityEvidenceService: ActivityEvidenceService,
    private val securityService: SecurityService,
    private val appProperties: AppProperties
){
    @Transactional
    @ReadOnly
    fun updateSubcontractedActivity(subcontractedActivityRequest: SubcontractedActivityRequestDTO, locale: Locale): SubcontractedActivityResponseDTO {
        securityService.checkSubcontractedActivityManagerRole()
        require(appProperties.binnacle.subcontractedUser.username != null){"A subcontracted user must be defined"}
        val userSubcontracted = userRepository.findByUsername(appProperties.binnacle.subcontractedUser.username.toString())?.toDomain()//desde un property

        require(userSubcontracted != null){"Subcontracted user must exist"}
        val projectRoleEntity = this.getProjectRoleEntity(subcontractedActivityRequest.projectRoleId)
        val projectRole = projectRoleEntity.toDomain()
        val currentActivity = this.getActivity(subcontractedActivityRequest.id!!)


        val activityToUpdate = activityRequestBodyConverter.toActivity(
                subcontractedActivityRequest,
                currentActivity.insertDate,
                projectRole,
                userSubcontracted
        )

        subcontractedActivityValidator.checkActivityIsValidForUpdate(activityToUpdate, currentActivity, userSubcontracted)

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

        return activityResponseConverter.toSubcontractedActivityResponseDTO(updatedActivity)
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