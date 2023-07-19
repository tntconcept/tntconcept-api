package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import com.autentia.tnt.security.application.canAccessAllActivities
import com.autentia.tnt.security.application.checkAuthentication
import io.micronaut.security.utils.SecurityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ActivityDeletionUseCase internal constructor(
        private val activityRepository: ActivityRepository,
        private val activityValidator: ActivityValidator,
        private val activityEvidenceService: ActivityEvidenceService,
        private val securityService: SecurityService
) {
    @Transactional
    @ReadOnly
    fun deleteActivityById(id: Long) {
        val activityToDelete = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        val authentication = securityService.checkAuthentication()

        if (authentication.canAccessAllActivities()) {
            activityValidator.checkAllAccessActivityIsValidForDeletion(activityToDelete.toDomain())
        } else {
            activityValidator.checkActivityIsValidForDeletion(activityToDelete.toDomain())
        }

        if (activityToDelete.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(id, activityToDelete.insertDate!!)
        }
        activityRepository.deleteById(id)
    }
}
