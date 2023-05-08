package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit

open class ActivityTimeInterval protected constructor(open val timeInterval: TimeInterval, val timeUnit: TimeUnit) {

    companion object {
        fun of(timeInterval: TimeInterval, timeUnit: TimeUnit) = ActivityTimeInterval(timeInterval, timeUnit)
    }

    fun isOneDay() = timeInterval.isOneDay()
    fun getDateInterval() = timeInterval.getDateInterval()

    fun getDurationByCountingWorkableDays(calendar: Calendar): Int =
        getDurationByCountingDays(calendar.getWorkableDays(getDateInterval()).size)

    fun getDurationByCountingDays(numberOfDays: Int) =
        if (timeUnit == TimeUnit.MINUTES) {
            timeInterval.getDurationInMinutes().toInt()
        } else {
            numberOfDays * 8 * 60
        }

    fun isInTheTimeInterval(timeInterval: TimeInterval) = timeInterval.isInTheTimeInterval(timeInterval)
}