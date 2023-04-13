package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivitiesResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivitiesResponseDTO
import com.autentia.tnt.binnacle.services.ActivitiesService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivitiesValidator
import jakarta.inject.Singleton
@Deprecated("Use ActivityRetrievalUseCase")
@Singleton
class ActivitiesRetrievalByIdUseCase internal constructor(
    private val activityService: ActivitiesService,
    private val userService: UserService,
    private val activityValidator: ActivitiesValidator,
    private val activityResponseConverter: ActivitiesResponseConverter
) {
    fun getActivityById(id: Long): ActivitiesResponseDTO? {
        val user = userService.getAuthenticatedUser()
        val activity = activityService.getActivityById(id)

        if (!activityValidator.userHasAccess(activity, user)) return null

        return activityResponseConverter.mapActivityToActivityResponseDTO(activity)
    }
}