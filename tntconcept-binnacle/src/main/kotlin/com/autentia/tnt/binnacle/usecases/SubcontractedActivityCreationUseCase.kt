package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class SubcontractedActivityCreationUseCase internal constructor (
        private val projectRoleRepository: ProjectRoleRepository,
        private val activityRepository: ActivityRepository,
        private val activityEvidenceService: ActivityEvidenceService,
        private val userService: UserService,
        private val subcontractedActivityValidator: SubcontractedActivityValidator,
        private val activityRequestBodyConverter: ActivityRequestBodyConverter,
        private val activityResponseConverter: ActivityResponseConverter,
        private val pendingApproveActivityMailUseCase: SendPendingApproveActivityMailUseCase,
        private val userRepository: UserRepository,
) {
    @Transactional
    fun createSubcontractedActivity(@Valid subcontractedActivityRequestBody: SubcontractedActivityRequestDTO, locale: Locale): SubcontractedActivityResponseDTO {
        val user = userService.getAuthenticatedDomainUser()
        val userSubcontracted = userRepository.find(5000)?.toDomain()
        if (userSubcontracted == null){
            throw IllegalStateException("User null")
        }
        val projectRole = this.getProjectRole(subcontractedActivityRequestBody.projectRoleId)

        val activityToCreate = activityRequestBodyConverter.toActivity(subcontractedActivityRequestBody, null, projectRole.toDomain(), userSubcontracted)

        subcontractedActivityValidator.checkActivityIsValidForCreation(activityToCreate, user)

        val savedActivity = activityRepository.save(Activity.of(activityToCreate, projectRole))

        if (activityToCreate.hasEvidences) {
            activityEvidenceService.storeActivityEvidence(savedActivity.id!!, activityToCreate.evidence!!, savedActivity.insertDate!!)
        }

        val savedActivityDomain = savedActivity.toDomain()

        if (savedActivityDomain.canBeApproved()) {
            pendingApproveActivityMailUseCase.send(savedActivityDomain, user.username, locale)
        }

        return activityResponseConverter.toSubcontractingActivityResponseDTO(savedActivityDomain)
    }

    private fun getProjectRole(projectRoleId: Long) = projectRoleRepository.findById(projectRoleId)
            ?: throw ProjectRoleNotFoundException(projectRoleId)


}