package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import java.time.Month
import kotlin.time.Duration

internal class WorkedTimeService(private val activityCalendarService: ActivityCalendarService) {

    fun workedTime(dateInterval: DateInterval, activities: List<Activity>): Map<Month, Duration> {
        val workableActivities = activities.filter { it.isWorkingTimeActivity() }
        return activityCalendarService.getActivityDurationByMonth(workableActivities, dateInterval)
    }

    fun getWorkedTimeByRoles(dateInterval: DateInterval, activities: List<Activity>): Map<Month, List<MonthlyRoles>> {
        val workableActivities = activities.filter { it.isWorkingTimeActivity() }
        return activityCalendarService.getActivityDurationByMonthlyRoles(workableActivities, dateInterval)
    }
}