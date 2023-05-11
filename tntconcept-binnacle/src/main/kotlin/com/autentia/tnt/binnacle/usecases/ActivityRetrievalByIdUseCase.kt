package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import jakarta.inject.Singleton

@Singleton
class ActivityRetrievalByIdUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityResponseConverter: ActivityResponseConverter
) {
    fun getActivityById(id: Long): ActivityResponseDTO? {
        val activity = activityService.getActivityById(id)
        return activityResponseConverter.toActivityResponseDTO(activity)
    }
}
