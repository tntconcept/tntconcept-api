package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit

private const val HOURS_BY_DAY = 8
private const val MINUTES_IN_HOUR = 60

data class TimeInfo(
    val maxTimeAllowed: MaxTimeAllowed,
    val timeUnit: TimeUnit,
) {
    fun getMaxTimeAllowedByYear() = maxTimeAllowed.byYear
    fun getMaxTimeAllowedByActivity() = maxTimeAllowed.byActivity

    fun getMaxTimeAllowedByYearInUnits(): Int {
        if (timeUnit === TimeUnit.DAYS || timeUnit === TimeUnit.NATURAL_DAYS) {
            return fromMinutesToDays(getMaxTimeAllowedByYear())
        }
        return getMaxTimeAllowedByYear()
    }

    fun getMaxTimeAllowedByActivityInUnits(): Int {
        if (timeUnit === TimeUnit.DAYS || timeUnit === TimeUnit.NATURAL_DAYS) {
            return fromMinutesToDays(getMaxTimeAllowedByActivity())
        }
        return getMaxTimeAllowedByActivity()
    }

    private fun fromMinutesToDays(minutes: Int) = minutes / (MINUTES_IN_HOUR * HOURS_BY_DAY)

}

data class MaxTimeAllowed(
    val byYear: Int,
    val byActivity: Int,
)

data class RemainingTimeInfo(
    val maxTimeAllowed: MaxTimeAllowed,
    val timeUnit: TimeUnit,
    val userRemainingTime: Int,
){
    fun getMaxTimeAllowedByYear() = maxTimeAllowed.byYear
    fun getMaxTimeAllowedByActivity() = maxTimeAllowed.byActivity

    companion object {
        fun of(timeInfo: TimeInfo, remaining:Int) =
            RemainingTimeInfo(MaxTimeAllowed(timeInfo.getMaxTimeAllowedByYearInUnits(), timeInfo.getMaxTimeAllowedByActivityInUnits()), timeInfo.timeUnit, remaining)
    }
}