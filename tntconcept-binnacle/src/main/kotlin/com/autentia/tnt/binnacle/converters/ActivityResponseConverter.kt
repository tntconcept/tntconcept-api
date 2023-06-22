package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import jakarta.inject.Singleton

@Singleton
class ActivityResponseConverter(
    private val activityIntervalResponseConverter: ActivityIntervalResponseConverter
) {

    fun toActivityResponseDTO(activity: com.autentia.tnt.binnacle.core.domain.Activity) = ActivityResponseDTO(
        billable = activity.billable,
        description = activity.description,
        hasEvidences = activity.hasEvidences,
        id = activity.id!!,
        projectRoleId = activity.projectRole.id,
        interval = activityIntervalResponseConverter.toIntervalResponseDTO(activity),
        userId = activity.userId,
        approvalState = activity.approvalState
    )

    fun mapActivityToActivityResponse(activity: Activity) = ActivityResponse(
        id = activity.id!!,
        start = activity.start,
        end = activity.end,
        billable = activity.billable,
        userId = activity.userId,
        description = activity.description,
        organization = activity.projectRole.project.organization,
        project = activity.projectRole.project,
        projectRole = activity.projectRole,
        duration = activity.duration,
        hasEvidences = activity.hasEvidences,
        approvalState = activity.approvalState
    )

    fun toActivityResponseDTO(activityResponse: ActivityResponse) =
        ActivityResponseDTO(
            activityResponse.billable,
            activityResponse.description,
            activityResponse.hasEvidences,
            activityResponse.id,
            activityResponse.projectRole.id,
            activityIntervalResponseConverter.mapActivityResponseToIntervalResponseDTO(activityResponse),
            activityResponse.userId,
            activityResponse.approvalState
        )
}