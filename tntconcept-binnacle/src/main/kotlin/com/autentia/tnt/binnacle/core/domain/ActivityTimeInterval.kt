package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.LocalDateTime
import java.time.LocalTime

open class ActivityTimeInterval protected constructor(open val timeInterval: TimeInterval, val timeUnit: TimeUnit) {

    companion object {
        fun of(timeInterval: TimeInterval, timeUnit: TimeUnit) =
            ActivityTimeInterval(
                TimeInterval.of(
                    getDateAtTimeIfNecessary(timeInterval.start, timeUnit, LocalTime.MIN),
                    getDateAtTimeIfNecessary(timeInterval.end, timeUnit, LocalTime.of(23, 59, 59))
                ),
                timeUnit
            )

        fun getDateAtTimeIfNecessary(
            date: LocalDateTime, timeUnit: TimeUnit, localTime: LocalTime
        ): LocalDateTime = if (timeUnit === TimeUnit.DAYS) date.toLocalDate().atTime(localTime) else date
    }

    fun isOneDay() = timeInterval.isOneDay()
    fun getDateInterval() = timeInterval.getDateInterval()

    fun getDurationByCountingWorkableDays(calendar: Calendar): Int =
        getDurationByCountingDays(calendar.getWorkableDays(getDateInterval()).size)

    fun getDuration(calendar: Calendar): Int {
        return when (timeUnit) {
            TimeUnit.MINUTES -> timeInterval.getDurationInMinutes().toInt()
            TimeUnit.NATURAL_DAYS -> getDurationByCountingDays(calendar.getAllDays(getDateInterval()).size)
            TimeUnit.DAYS -> getDurationByCountingDays(calendar.getWorkableDays(getDateInterval()).size)
        }
    }

    fun getDurationByCountingDays(numberOfDays: Int) =
        if (timeUnit == TimeUnit.MINUTES) {
            timeInterval.getDurationInMinutes().toInt()
        } else {
            numberOfDays * 8 * 60
        }

    fun isInTheTimeInterval(requestedTimeInterval: TimeInterval) = timeInterval.isInTheTimeInterval(requestedTimeInterval)
}