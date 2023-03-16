package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import jakarta.inject.Singleton

@Singleton
open class ActivityApprovalUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityResponseConverter: ActivityResponseConverter
) {
    fun approveActivity(id: Long): ActivityResponseDTO {
        //TODO: VALIDATE APPROVAL PERMISSIONS
        val activity = activityService.approveActivityById(id)
        return activityResponseConverter.mapActivityToActivityResponseDTO(activity)
    }
}