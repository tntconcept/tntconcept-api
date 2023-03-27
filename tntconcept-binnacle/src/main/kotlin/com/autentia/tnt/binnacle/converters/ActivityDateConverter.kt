package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivitiesResponse
import com.autentia.tnt.binnacle.core.domain.ActivityDate
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.core.utils.myDatesUntil
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import jakarta.inject.Singleton
import java.time.LocalDate

@Deprecated("Used in the deprecated Activities Controller")
@Singleton
class ActivityDateConverter(
    private val workableProjectRoleIdChecker: WorkableProjectRoleIdChecker,
    private val activityResponseConverter: ActivitiesResponseConverter
) {

    fun toActivityDateDTO(activityDate: ActivityDate) =
        ActivityDateDTO(
            activityDate.date,
            activityDate.workedMinutes,
            activityDate.activities.map { activityResponseConverter.toActivityResponseDTO(it) }
        )

    fun toListActivityDate(
        activities: List<ActivitiesResponse>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<ActivityDate> {
        val activitiesByDate = activities.groupBy { it.startDate.toLocalDate() }
        val allActivitiesBetweenDates = startDate.myDatesUntil(endDate)
            .associateWith { emptyList<ActivitiesResponse>() }
            .toMutableMap()
        allActivitiesBetweenDates.putAll(activitiesByDate)
        return allActivitiesBetweenDates.map(::toActivityDate)
    }

    private fun toActivityDate(entry: Map.Entry<LocalDate, List<ActivitiesResponse>>): ActivityDate =
        ActivityDate(
            date = entry.key,
            workedMinutes = entry.value
                .filter { workableProjectRoleIdChecker.isWorkable(ProjectRoleId(it.projectRole.id)) }
                .fold(0) { acc, activity -> acc + activity.duration },
            activities = entry.value.sortedBy { it.startDate }
        )

}