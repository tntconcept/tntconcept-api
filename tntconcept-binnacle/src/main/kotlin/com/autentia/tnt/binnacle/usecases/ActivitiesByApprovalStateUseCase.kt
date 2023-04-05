package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import jakarta.inject.Singleton

@Singleton
class ActivitiesByApprovalStateUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityResponseConverter: ActivityResponseConverter
) {
    fun getActivities(approvalState: ApprovalState): List<ActivityResponseDTO> {
        val activities = activityService.getActivitiesApprovalState(approvalState)
        return activityResponseConverter.mapActivitiesToActivitiesResponseDTO(activities)
    }
}