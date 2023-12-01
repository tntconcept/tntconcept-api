package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import jakarta.inject.Singleton

@Singleton
class ActivityIntervalResponseConverter {
    fun toIntervalResponseDTO(activity: com.autentia.tnt.binnacle.core.domain.Activity) = IntervalResponseDTO(
        start = activity.getStart(),
        end = activity.getEnd(),
        duration = activity.getDurationInUnits(),
        timeUnit = activity.timeUnit
    )

    fun mapActivityResponseToIntervalResponseDTO(activityResponse: ActivityResponse) = IntervalResponseDTO(
        start = activityResponse.start,
        end = activityResponse.end,
        duration = intervalDurationToTimeUnit(activityResponse),
        timeUnit = activityResponse.projectRole.timeUnit
    )

    private fun intervalDurationToTimeUnit(activityResponse: ActivityResponse): Int {
        val durationToDays = TimeInterval.of(activityResponse.start, activityResponse.end).getDuration().toDays().toInt()
        return if (activityResponse.projectRole.timeUnit == TimeUnit.DAYS) durationToDays else activityResponse.duration
    }

}