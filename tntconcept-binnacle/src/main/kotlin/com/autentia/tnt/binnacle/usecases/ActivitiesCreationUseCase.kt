package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivitiesRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivitiesResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivitiesRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivitiesResponseDTO
import com.autentia.tnt.binnacle.services.ActivitiesService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivitiesValidator
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import jakarta.validation.Valid

@Deprecated("Use ActivityCreationUseCase instead")
@Singleton
@Validated
open class ActivitiesCreationUseCase internal constructor(
    private val activityService: ActivitiesService,
    private val userService: UserService,
    private val activityValidator: ActivitiesValidator,
    private val activityRequestBodyConverter: ActivitiesRequestBodyConverter,
    private val activityResponseConverter: ActivitiesResponseConverter
) {

    fun createActivity(@Valid activityRequestBody: ActivitiesRequestBodyDTO): ActivitiesResponseDTO {
        val user = userService.getAuthenticatedUser()
        val activityRequest = activityRequestBodyConverter
            .mapActivityRequestBodyDTOToActivityRequestBody(activityRequestBody)

        activityValidator.checkActivityIsValidForCreation(activityRequest, user)
        val activityResponse = activityResponseConverter.mapActivityToActivityResponse(
            activityService.createActivity(
                activityRequest,
                user
            )
        )

        return activityResponseConverter.toActivityResponseDTO(activityResponse)
    }

}