package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import jakarta.inject.Singleton

@Singleton
class ActivityRetrievalByIdUseCase internal constructor(
    private val activityService: ActivityService,
    private val userService: UserService,
    private val activityValidator: ActivityValidator,
    private val activityResponseConverter: ActivityResponseConverter
) {
    fun getActivityById(id: Long): ActivityResponseDTO? {
        val user = userService.getAuthenticatedUser()
        val activity = activityService.getActivityById(id)

        if (!activityValidator.userHasAccess(activity, user)) return null

        return activityResponseConverter.mapActivityToActivityResponseDTO(activity)
    }
}
