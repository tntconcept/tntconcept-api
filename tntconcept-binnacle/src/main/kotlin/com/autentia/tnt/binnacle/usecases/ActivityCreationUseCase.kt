package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.ActivityTimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class ActivityCreationUseCase internal constructor(
        private val projectRoleRepository: ProjectRoleRepository,
        private val activityRepository: ActivityRepository,
        private val attachmentInfoRepository: AttachmentInfoRepository,
        private val activityCalendarService: ActivityCalendarService,
        private val userService: UserService,
        private val activityValidator: ActivityValidator,
        private val activityRequestBodyConverter: ActivityRequestBodyConverter,
        private val activityResponseConverter: ActivityResponseConverter,
        private val pendingApproveActivityMailUseCase: SendPendingApproveActivityMailUseCase,
        private val dateService: DateService
) {

    @Transactional
    fun createActivity(@Valid activityRequestBody: ActivityRequestDTO, locale: Locale): ActivityResponseDTO {
        val user = userService.getAuthenticatedDomainUser()
        val projectRole = this.getProjectRole(activityRequestBody.projectRoleId)

        val duration = activityCalendarService.getDurationByCountingWorkingDays(
                ActivityTimeInterval.of(activityRequestBody.interval.toDomain(), projectRole.timeUnit)
        )

        val activityToCreate = activityRequestBodyConverter
                .toActivity(activityRequestBody, duration, dateService.getDateNow(), projectRole.toDomain(), user)

        activityValidator.checkActivityIsValidForCreation(activityToCreate, user)

        val savedActivity = this.createAndSaveActivityEntity(activityToCreate, projectRole)

        val savedActivityDomain = savedActivity.toDomain()

        if (savedActivityDomain.canBeApproved()) {
            pendingApproveActivityMailUseCase.send(savedActivityDomain, user.username, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(savedActivityDomain)
    }

    private fun createAndSaveActivityEntity(activityToCreate: com.autentia.tnt.binnacle.core.domain.Activity,
                                            projectRole: ProjectRole): Activity {
        val activityEvidences = attachmentInfoRepository.findByIds(activityToCreate.evidences)
        val activityEntityToCreate = Activity.of(activityToCreate, projectRole, activityEvidences.map { AttachmentInfo.of(it) }.toMutableList())
        val activityEntity = activityRepository.save(activityEntityToCreate)
        val updatedEvidences = activityEvidences.map { it.copy(isTemporary = false) }
        attachmentInfoRepository.save(updatedEvidences)
        return activityEntity
    }

    private fun getProjectRole(projectRoleId: Long) = projectRoleRepository.findById(projectRoleId)
            ?: throw ProjectRoleNotFoundException(projectRoleId)
}