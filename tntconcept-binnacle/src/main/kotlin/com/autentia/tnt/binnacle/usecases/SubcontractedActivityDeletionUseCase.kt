package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.validators.SubcontractedActivityValidator
import com.autentia.tnt.security.application.checkSubcontractedActivityManagerRole
import io.micronaut.security.utils.SecurityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class SubcontractedActivityDeletionUseCase internal constructor(
        private val activityRepository: ActivityRepository,
        private val subcontractedActivityValidator: SubcontractedActivityValidator,
        private val activityEvidenceService: ActivityEvidenceService,
        private val securityService: SecurityService,
        private val userRepository: UserRepository
) {
    @Transactional
    @ReadOnly
    fun deleteSubcontractedActivityById(id: Long) {
        securityService.checkSubcontractedActivityManagerRole()
        val activityToDelete = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        val userSubcontracted = userRepository.findByUsername("subcontracted")?.toDomain()//desde un property
        require(userSubcontracted != null) { "Subcontracted user must exist" }
        require(activityToDelete.userId == userSubcontracted.id) { "User cannot delete activity others than subcontracted user ones" }

        subcontractedActivityValidator.checkActivityIsValidForDeletion(activityToDelete.toDomain())

        if (activityToDelete.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(id, activityToDelete.insertDate!!)
        }

        activityRepository.deleteById(id)
    }
}