package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.core.domain.ActivitySummary
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.core.utils.myDatesUntil
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ActivitySummaryDTO
import jakarta.inject.Singleton
import java.time.LocalDate


@Singleton
class ActivitySummaryConverter (private val workableProjectRoleIdChecker: WorkableProjectRoleIdChecker)
{

    fun toActivitySummaryDTO(activitySummary: ActivitySummary) =
        ActivitySummaryDTO(
            activitySummary.date,
            activitySummary.workedHours,
        )

    fun toListActivitySummaryDate(
        activities: List<ActivityResponse>,
        start: LocalDate,
        end: LocalDate,
    ): List<ActivitySummary> {
        val allActivitiesBetweenDates = start.myDatesUntil(end)
            .associateWith { 0.0 }
            .toMutableMap()
        activities
            .filter { workableProjectRoleIdChecker.isWorkable(ProjectRoleId(it.projectRole.id)) }
            .forEach { activity: ActivityResponse ->
            if(activity.projectRole.timeUnit == TimeUnit.DAYS) {
                listActivitiesDays(activity.start.toLocalDate(), activity.end.toLocalDate()).forEach { day: LocalDate ->
                    allActivitiesBetweenDates[day] = allActivitiesBetweenDates.getOrDefault(day, 0.0) + 8
                }
            } else {
                allActivitiesBetweenDates[activity.start.toLocalDate()] = allActivitiesBetweenDates.getOrDefault(activity.start.toLocalDate(), 0.0) + (activity.duration / 60)
            }
        }
        return allActivitiesBetweenDates.map(::toActivitySummary)
    }

    private fun toActivitySummary(entry: Map.Entry<LocalDate, Double>): ActivitySummary =
        ActivitySummary(
            date = entry.key,
            workedHours = entry.value
        )

    private fun listActivitiesDays(start: LocalDate, end: LocalDate): List<LocalDate> = generateSequence(start) {
            d -> d.plusDays(1).takeIf { it <= end }
    }.toList()

}