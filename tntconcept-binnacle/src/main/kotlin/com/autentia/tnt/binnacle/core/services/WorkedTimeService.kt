package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import java.time.Month
import kotlin.time.Duration

internal class WorkedTimeService(
    private val activityCalendarService: ActivityCalendarService,
    private val workableProjectRoleIdChecker: WorkableProjectRoleIdChecker
) {

    fun workedTime(dateInterval: DateInterval, activities: List<Activity>): Map<Month, Duration> {
        val workableActivities =
            activities.filter { workableProjectRoleIdChecker.isWorkable(ProjectRoleId(it.projectRole.id)) }
        return activityCalendarService.getActivityDurationByMonth(workableActivities, dateInterval)
    }

    fun getWorkedTimeByRoles(dateInterval: DateInterval, activities: List<Activity>): Map<Month, List<MonthlyRoles>> {
        val workableActivities =
            activities.filter { workableProjectRoleIdChecker.isWorkable(ProjectRoleId(it.projectRole.id)) }
        return activityCalendarService.getActivityDurationByMonthlyRoles(workableActivities, dateInterval)
    }
}