package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import jakarta.inject.Singleton

@Singleton
class ActivityResponseConverter(
    private val activityIntervalResponseConverter: ActivityIntervalResponseConverter
) {

    fun mapActivityToActivityResponseDTO(activity: Activity) = ActivityResponseDTO(
        billable = activity.billable,
        description = activity.description,
        hasEvidences = activity.hasEvidences,
        id = activity.id!!,
        projectRoleId = activity.projectRole.id,
        interval = activityIntervalResponseConverter.mapActivityToIntervalResponseDTO(activity),
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

    fun toActivity(activity: Activity) =
        com.autentia.tnt.binnacle.core.domain.Activity(
            activity.start,
            activity.end,
            ProjectRole(activity.projectRole.id, activity.projectRole.timeUnit)
        )

    fun mapActivitiesToActivitiesResponseDTO(activities: List<Activity>): List<ActivityResponseDTO>  {
        val activitiesResponseDTO = mutableListOf<ActivityResponseDTO>()
        activities.forEach {activity ->

            val activityResponseDTO = ActivityResponseDTO(
                billable = activity.billable,
                description = activity.description,
                hasEvidences = activity.hasEvidences,
                id = activity.id!!,
                projectRoleId = activity.projectRole.id,
                interval = activityIntervalResponseConverter.mapActivityToIntervalResponseDTO(activity),
                userId = activity.userId,
                approvalState = activity.approvalState
            )
            activitiesResponseDTO.add(activityResponseDTO)
        }
        return activitiesResponseDTO
    }

}
