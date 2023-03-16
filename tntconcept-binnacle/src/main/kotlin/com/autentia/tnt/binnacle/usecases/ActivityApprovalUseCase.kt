package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import jakarta.inject.Singleton

@Singleton
open class ActivityApprovalUseCase internal constructor(
    private val activityService: ActivityService,
    private val userService: UserService,
    private val activityValidator: ActivityValidator,
    private val activityResponseConverter: ActivityResponseConverter
) {
    fun approveActivity(id: Long): ActivityResponseDTO {
        val user = userService.getAuthenticatedUser()
        activityValidator.checkIfUserCanApproveActivity(user, id)
        val activity = activityService.approveActivityById(id)
        return activityResponseConverter.mapActivityToActivityResponseDTO(activity)
    }
}