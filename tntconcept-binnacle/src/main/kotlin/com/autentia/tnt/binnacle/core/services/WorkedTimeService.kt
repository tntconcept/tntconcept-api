package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import java.time.Month
import kotlin.time.Duration

class WorkedTimeService(private val workableProjectRoleIdChecker: WorkableProjectRoleIdChecker) {

    fun workedTime(activities: List<Activity>): Map<Month, Duration> {
        return activities
            .filter { workableProjectRoleIdChecker.isWorkable(it.projectRole) }
            .groupBy { it.date.month }
            .mapValues {
                it.value
                    .map { activity -> activity.duration }
                    .fold(Duration.ZERO, Duration::plus)
            }
    }

}
