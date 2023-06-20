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
    val maxAllowed: Int,
    val timeUnit: TimeUnit,
    val isWorkingTime: Boolean,
    val isApprovalRequired: Boolean
) {
    fun getRemainingInUnits(calendar: Calendar, activities: List<Activity>): Int {
        val remaining = getRemaining(calendar, activities)
        if (timeUnit === TimeUnit.DAYS || timeUnit === TimeUnit.NATURAL_DAYS) {
            return fromMinutesToDays(remaining)
        }
        return remaining
    }

    fun getMaxAllowedInUnits(): Int {
        if (timeUnit === TimeUnit.DAYS || timeUnit === TimeUnit.NATURAL_DAYS) {
            return fromMinutesToDays(maxAllowed)
        }
        return maxAllowed
    }

    fun getApprovalState() = if (isApprovalRequired) ApprovalState.PENDING else ApprovalState.NA

    private fun getRemaining(calendar: Calendar, activities: List<Activity>) =
        if (maxAllowed == 0) {
            0
        } else {
            maxAllowed - activities.sumOf { activity -> activity.getDuration(calendar) }
        }

    private fun fromMinutesToDays(minutes: Int) = minutes / (MINUTES_IN_HOUR * HOURS_BY_DAY)
}