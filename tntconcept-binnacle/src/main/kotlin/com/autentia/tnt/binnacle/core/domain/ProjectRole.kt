package com.autentia.tnt.binnacle.core.domain


import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit

private const val HOURS_BY_DAY = 8
private const val MINUTES_IN_HOUR = 60

data class ProjectRole(
    val id: Long,
    val name: String,
    val requireEvidence: RequireEvidence,
    val project: Project,
    val isWorkingTime: Boolean,
    val isApprovalRequired: Boolean,
    val timeInfo: TimeInfo,
) {
    fun getRemainingInUnits(calendar: Calendar, activities: List<Activity>): Int {
        val remaining = getRemaining(calendar, activities)
        if (timeInfo.timeUnit === TimeUnit.DAYS || timeInfo.timeUnit === TimeUnit.NATURAL_DAYS) {
            return fromMinutesToDays(remaining)
        }
        return remaining
    }

    fun getMaxTimeAllowedByYear() = timeInfo.maxTimeAllowedByYear

    fun getMaxTimeAllowedByActivity() = timeInfo.maxTimeAllowedByYear

    fun getTimeUnit() = timeInfo.timeUnit

    fun getMaxTimeAllowedByYearInUnits(): Int {
        if (timeInfo.timeUnit === TimeUnit.DAYS || timeInfo.timeUnit === TimeUnit.NATURAL_DAYS) {
            return fromMinutesToDays(timeInfo.maxTimeAllowedByYear)
        }
        return timeInfo.maxTimeAllowedByYear
    }

    fun getApprovalState() = if (isApprovalRequired) ApprovalState.PENDING else ApprovalState.NA

    private fun getRemaining(calendar: Calendar, activities: List<Activity>) =
        if (timeInfo.maxTimeAllowedByYear == 0) {
            0
        } else {
            timeInfo.maxTimeAllowedByYear - activities.sumOf { activity -> activity.getDuration(calendar) }
        }

    private fun fromMinutesToDays(minutes: Int) = minutes / (MINUTES_IN_HOUR * HOURS_BY_DAY)
}