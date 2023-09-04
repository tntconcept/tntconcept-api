package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityTimeInterval
import com.autentia.tnt.binnacle.core.domain.AttachmentInfoId
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.validators.ActivityValidator
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
        private val sendPendingApproveActivityMailUseCase: SendPendingApproveActivityMailUseCase,
        private val attachmentInfoRepository: AttachmentInfoRepository
) {

    @Transactional
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

        this.markOutdatedAttachments(currentActivity.evidences)

        val updatedActivityEntity = this.updateActivityEntity(activityToUpdate, projectRoleEntity)

        val updatedActivity = updatedActivityEntity.toDomain()

        if (updatedActivity.canBeApproved()) {
            sendPendingApproveActivityMailUseCase.send(updatedActivity, user.username, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(updatedActivity)
    }

    private fun updateActivityEntity(activityToUpdate: com.autentia.tnt.binnacle.core.domain.Activity,
                                     projectRole: ProjectRole): Activity {
        val activityEvidences = attachmentInfoRepository.findByIds(activityToUpdate.evidences)
        val activityEntityToCreate = Activity.of(activityToUpdate, projectRole, activityEvidences.map { AttachmentInfo.of(it) }.toMutableList())
        val activityEntity = activityRepository.save(activityEntityToCreate)
        val updatedEvidences = activityEvidences.map { it.copy(isTemporary = false) }
        attachmentInfoRepository.save(updatedEvidences)
        return activityEntity
    }

    private fun markOutdatedAttachments(evidences: List<AttachmentInfoId>) {
        val attachments = attachmentInfoRepository.findByIds(evidences)
        val attachmentsUpdated = attachments.map { it.copy(isTemporary = true) }
        attachmentInfoRepository.save(attachmentsUpdated)
    }

    private fun getProjectRoleEntity(projectRoleId: Long) =
            projectRoleRepository.findById(projectRoleId) ?: throw ProjectRoleNotFoundException(projectRoleId)

    private fun getActivity(activityId: Long) =
            Optional.ofNullable(activityRepository.findById(activityId)).orElseThrow { ActivityNotFoundException(activityId) }.toDomain()
}