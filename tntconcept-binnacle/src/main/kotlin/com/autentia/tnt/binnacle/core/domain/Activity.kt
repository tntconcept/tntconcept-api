package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.LocalDateTime


data class Activity(val start: LocalDateTime, val end: LocalDateTime, val projectRole: ProjectRole, val userId: Long) {
    private fun getTimeInterval() = TimeInterval.of(start, end)
    fun isOneDay() = start.toLocalDate().isEqual(end.toLocalDate())
    fun getDateInterval() = DateInterval.of(start.toLocalDate(), end.toLocalDate())
    fun getDurationByCountingWorkableDays(calendar: Calendar): Int =
        getDurationByCountingDays(calendar.getWorkableDays(getDateInterval()).size)

    fun getDurationByCountingDays(numberOfDays: Int) =
        if (projectRole.timeUnit == TimeUnit.MINUTES) {
            getTimeInterval().getDuration().toMinutes().toInt()
        } else {
            numberOfDays * 8 * 60
        }

    fun isInTheTimeInterval(timeInterval: TimeInterval) =
          (start.isBefore(timeInterval.end) || start.isEqual(timeInterval.end))
                    && (end.isAfter(timeInterval.start) || end.isEqual(timeInterval.start))
}