package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.services.ActivitiesService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivitiesValidator
import jakarta.inject.Singleton
@Deprecated("Use ActivityDeletionUseCase instead")
@Singleton
class ActivitiesDeletionUseCase internal constructor(
    private val activityService: ActivitiesService,
    private val userService: UserService,
    private val activityValidator: ActivitiesValidator
)  {
    fun deleteActivityById(id: Long) {
        val user = userService.getAuthenticatedUser()
        activityValidator.checkActivityIsValidForDeletion(id, user)
        activityService.deleteActivityById(id)
    }
}