package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import jakarta.inject.Singleton

@Singleton
class ActivityDeletionUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityValidator: ActivityValidator
)  {
    fun deleteActivityById(id: Long) {
        activityValidator.checkActivityIsValidForDeletion(id)
        activityService.deleteActivityById(id)
    }
}
