package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityTimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityEvidenceMailService
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.UserService
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
    private val activityEvidenceMailService: ActivityEvidenceMailService,
    private val activityEvidenceService: ActivityEvidenceService,
) {
    @Transactional
    @ReadOnly
    fun updateActivity(activityRequest: ActivityRequestDTO, locale: Locale): ActivityResponseDTO {

        val user = userService.getAuthenticatedDomainUser()
        val projectRoleEntity =
            projectRoleRepository.findById(activityRequest.projectRoleId)
                ?: throw ProjectRoleNotFoundException(
                    activityRequest.projectRoleId
                )
        val projectRole = projectRoleEntity.toDomain()
        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            ActivityTimeInterval.of(activityRequest.interval.toDomain(), projectRoleEntity.timeUnit)
        )

        val currentActivityEntity =
            activityRepository.findById(activityRequest.id!!) ?: throw ActivityNotFoundException(activityRequest.id)

        val currentActivity = currentActivityEntity.toDomain()

        val activityToUpdate = activityRequestBodyConverter.toActivity(
            activityRequest,
            duration,
            currentActivity.insertDate,
            projectRole,
            user
        )

        activityValidator.checkActivityIsValidForUpdate(activityToUpdate, currentActivity, user)

        val updatedActivityEntity = activityRepository.update(Activity.of(activityToUpdate, projectRoleEntity))

        val updatedActivity = updatedActivityEntity.toDomain();

        if (activityToUpdate.hasEvidences) {
            activityEvidenceService.storeActivityEvidence(
                updatedActivityEntity.id!!,
                activityToUpdate.evidence!!,
                updatedActivityEntity.insertDate!!
            )
        }

        if (!activityToUpdate.hasEvidences && currentActivityEntity.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(
                updatedActivityEntity.id!!,
                updatedActivityEntity.insertDate!!
            )
        }

        if (updatedActivity.activityCanBeApproved()) {
            activityEvidenceMailService.sendActivityEvidenceMail(updatedActivity, user.username, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(updatedActivity)
    }
}