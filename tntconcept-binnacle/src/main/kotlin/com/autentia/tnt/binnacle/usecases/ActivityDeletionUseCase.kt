package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import jakarta.inject.Singleton

@Singleton
class ActivityDeletionUseCase internal constructor(
    private val activityRepository: ActivityRepository,
    private val activityValidator: ActivityValidator,
    private val activityEvidenceService: ActivityEvidenceService,
) {
    fun deleteActivityById(id: Long) {
        val activityToDelete = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        activityValidator.checkActivityIsValidForDeletion(activityToDelete.toDomain())
        if (activityToDelete.hasEvidences) {
            activityEvidenceService.deleteActivityEvidence(id, activityToDelete.insertDate!!)
        }
        activityRepository.deleteById(id)
    }
}
