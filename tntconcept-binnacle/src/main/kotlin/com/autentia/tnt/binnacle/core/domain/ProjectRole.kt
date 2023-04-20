package com.autentia.tnt.binnacle.core.domain


import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit

data class ProjectRole(
    val id: Long,
    val name: String,
    val project: Project,
    val maxAllowed: Int,
    val timeUnit: TimeUnit,
    val requireEvidence: RequireEvidence,
    val isApprovalRequired: Boolean
) {
    fun getRemainingInUnits(calendar: Calendar, activities: List<Activity>): Int {
        val remaining = getRemaining(calendar, activities)
        if (timeUnit === TimeUnit.DAYS) {
            return remaining / (60 * 8)
        }
        return remaining
    }

    fun getMaxAllowedInUnits(): Int {
        if (timeUnit === TimeUnit.DAYS) {
            return maxAllowed / (60 * 8)
        }
        return maxAllowed
    }

    private fun getRemaining(calendar: Calendar, activities: List<Activity>) =
        if (maxAllowed == 0) {
            0
        } else {
            maxAllowed - activities.sumOf { activity -> activity.getDurationByCountingWorkableDays(calendar) }
        }
}