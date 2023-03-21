package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import jakarta.inject.Singleton

@Singleton
class ActivityDateConverter(
        private val activityIntervalResponseConverter: ActivityIntervalResponseConverter
    ) {

    fun mapActivitiesToActivitiesDateDTO(activities: List<Activity>): List<ActivityDateDTO>  {
        val activitiesDateDTO = mutableListOf<ActivityDateDTO>()
        activities.forEach {activity ->

        val activityDateDTO = ActivityDateDTO(
            billable = activity.billable,
            description = activity.description,
            hasEvidences = activity.hasEvidences,
            id = activity.id!!,
            projectRoleId = activity.projectRole.id,
            interval = activityIntervalResponseConverter.mapActivityToIntervalResponseDTO(activity),
            userId = activity.userId,
            approvalState = activity.approvalState
            )
            activitiesDateDTO.add(activityDateDTO)
        }
        return activitiesDateDTO
    }
}

