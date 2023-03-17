package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import jakarta.inject.Singleton

@Singleton
class ActivityDateConverter {
    fun mapActivityToActivityDateDTO(activity: Activity) = ActivityDateDTO(
        billable = activity.billable,
        description = activity.description,
        hasEvidences = activity.hasEvidences,
        id = activity.id!!,
        projectRoleId = activity.projectRole.id,
        interval = mapActivityToIntervalResponseDTO(activity),
        userId = activity.userId,
        approvalState = activity.approvalState
    )

    fun mapActivitiesToActivitiesDateDTO(activities: List<Activity>): List<ActivityDateDTO>  {
        val activitiesDateDTO = mutableListOf<ActivityDateDTO>()
        activities.forEach {activity ->

        val activityDateDTO = ActivityDateDTO(
            billable = activity.billable,
            description = activity.description,
            hasEvidences = activity.hasEvidences,
            id = activity.id!!,
            projectRoleId = activity.projectRole.id,
            interval = mapActivityToIntervalResponseDTO(activity),
            userId = activity.userId,
            approvalState = activity.approvalState
            )
            activitiesDateDTO.add(activityDateDTO)
        }
        return activitiesDateDTO
    }

    fun mapActivityToIntervalResponseDTO(activity: Activity) = IntervalResponseDTO(
        start = activity.start,
        end = activity.end,
        duration = activity.duration,
        timeUnit = activity.projectRole.timeUnit

    )
}

