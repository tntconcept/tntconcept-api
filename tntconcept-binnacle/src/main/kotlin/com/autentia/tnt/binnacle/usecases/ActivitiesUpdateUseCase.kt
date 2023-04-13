package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivitiesRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivitiesResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivitiesRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivitiesResponseDTO
import com.autentia.tnt.binnacle.services.ActivitiesService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivitiesValidator
import jakarta.inject.Singleton

@Deprecated("Use ActivityUpdateUseCase")
@Singleton
class ActivitiesUpdateUseCase internal constructor(
    private val activityService: ActivitiesService,
    private val userService: UserService,
    private val activityValidator: ActivitiesValidator,
    private val activityRequestBodyConverter: ActivitiesRequestBodyConverter,
    private val activityResponseConverter: ActivitiesResponseConverter
) {
    fun updateActivity(activityRequest: ActivitiesRequestBodyDTO): ActivitiesResponseDTO {
        val user = userService.getAuthenticatedUser()
        val  activityRequestBody = activityRequestBodyConverter.mapActivityRequestBodyDTOToActivityRequestBody(activityRequest)
        activityValidator.checkActivityIsValidForUpdate(activityRequestBody, user)
        return activityResponseConverter.mapActivityToActivityResponseDTO(activityService.updateActivity(activityRequestBody, user))
    }
}