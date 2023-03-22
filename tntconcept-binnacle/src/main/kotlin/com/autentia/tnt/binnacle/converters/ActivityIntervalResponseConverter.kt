package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import jakarta.inject.Singleton

@Singleton
class ActivityIntervalResponseConverter {

    fun mapActivityToIntervalResponseDTO(activity: Activity) = IntervalResponseDTO(
        start = activity.start,
        end = activity.end,
        duration = activity.duration,
        timeUnit = activity.projectRole.timeUnit
    )

    fun mapActivityResponseToIntervalResponseDTO(activityResponse: ActivityResponse) = IntervalResponseDTO(
        start = activityResponse.start,
        end = activityResponse.end,
        duration = activityResponse.duration,
        timeUnit = activityResponse.projectRole.timeUnit

    )
}
