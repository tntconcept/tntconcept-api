package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyHookDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.HookActivityService
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class ActivityCreationHookUseCase internal constructor(
    private val activityService: HookActivityService,
    private val userService: UserService,
    private val activityRequestBodyConverter: ActivityRequestBodyConverter,
    private val activityResponseConverter: ActivityResponseConverter
) {

    fun createActivity(@Valid activityRequestBody: ActivityRequestBodyHookDTO): ActivityResponseDTO {
        val user = userService.getByUserName(activityRequestBody.userName)
        val activityRequest = activityRequestBodyConverter
            .mapActivityRequestBodyDTOToActivityRequestBody(activityRequestBody)

        val activityResponse = if (activityRequestBody.id == null) {
            activityResponseConverter.mapActivityToActivityResponse(
                activityService.createActivity(
                    activityRequest,
                    user
                )
            )
        } else {
            activityResponseConverter.mapActivityToActivityResponse(
                activityService.updateActivity(
                    activityRequest,
                    user
                )
            )
        }

        return activityResponseConverter.toActivityResponseDTO(activityResponse)
    }

}
