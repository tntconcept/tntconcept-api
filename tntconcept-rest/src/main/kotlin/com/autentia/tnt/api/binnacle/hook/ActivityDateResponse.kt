package com.autentia.tnt.api.binnacle.hook

import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import java.time.LocalDate

data class ActivityDateResponse(
    val date: LocalDate,
    val workedMinutes: Int,
    val activities: List<ActivityResponse>,
) {
    companion object {
        fun from(activityDateDTO: ActivityDateDTO) =
            ActivityDateResponse(
                activityDateDTO.date,
                activityDateDTO.workedMinutes,
                activityDateDTO.activities.map { ActivityResponse.from(it) }
            )
    }
}