package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
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
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import com.autentia.tnt.security.application.checkSubcontractedActivityManagerRole
import io.micronaut.security.utils.SecurityService
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class SubcontractedActivityCreationUseCase internal constructor(
    private val projectRoleRepository: ProjectRoleRepository,
    private val activityRepository: ActivityRepository,
    private val activityEvidenceService: ActivityEvidenceService,
    private val subcontractedActivityValidator: SubcontractedActivityValidator,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter,
    private val userRepository: UserRepository,
    private val securityService: SecurityService,
    private val appProperties: AppProperties
) {
    @Transactional
    fun createSubcontractedActivity(@Valid subcontractedActivityRequestBody: SubcontractedActivityRequestDTO, locale: Locale): SubcontractedActivityResponseDTO {
        securityService.checkSubcontractedActivityManagerRole()

        val userSubcontracted = userRepository.findByUsername(appProperties.binnacle.subcontractedUser.username.toString())?.toDomain()//desde un property

        require(userSubcontracted != null){"Subcontracted user must exist"}
        val projectRole = this.getProjectRole(subcontractedActivityRequestBody.projectRoleId)

        val activityToCreate = activityRequestBodyConverter.toActivity(subcontractedActivityRequestBody, null, projectRole.toDomain(), userSubcontracted)



        subcontractedActivityValidator.checkActivityIsValidForCreation(activityToCreate, userSubcontracted)

        val savedActivity = activityRepository.save(Activity.of(activityToCreate, projectRole))

        if (activityToCreate.hasEvidences) {
            activityEvidenceService.storeActivityEvidence(savedActivity.id!!, activityToCreate.evidence!!, savedActivity.insertDate!!)
        }

        val savedActivityDomain = savedActivity.toDomain()


        return activityResponseConverter.toSubcontractedActivityResponseDTO(savedActivityDomain)
    }

    private fun getProjectRole(projectRoleId: Long) = projectRoleRepository.findById(projectRoleId)
            ?: throw ProjectRoleNotFoundException(projectRoleId)


}