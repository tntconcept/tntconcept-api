package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.Calendar
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DailyWorkingTime
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.TimeUnit
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.transaction.Transactional

@Singleton
internal class ActivityCalendarService(
    private val activityService: ActivityService,
    private val calendarFactory: CalendarFactory,
    private val activitiesCalendarFactory: ActivitiesCalendarFactory,
) {

    @Transactional
    @ReadOnly
    fun createCalendar(dateInterval: DateInterval) = calendarFactory.create(dateInterval)

    @Transactional
    @ReadOnly
    fun getActivityDurationSummaryInHours(dateInterval: DateInterval, userId: Long): List<DailyWorkingTime> {
        val activities = activityService.getActivitiesBetweenDates(dateInterval, userId)
        val activitiesCalendar = activitiesCalendarFactory.create(dateInterval)

        activitiesCalendar.addAllActivities(activities)
        return activitiesCalendar.activitiesCalendarMap.toList().map {
            toDailyWorkingTime(it)
        }
    }

    private fun toDailyWorkingTime(activitiesInDate: Pair<LocalDate, List<Activity>>): DailyWorkingTime {
        return if (activitiesInDate.second.isNotEmpty()) {
            DailyWorkingTime(
                activitiesInDate.first,
                getActivitiesDuration(activitiesInDate.second)
            )
        } else{
            DailyWorkingTime(
                activitiesInDate.first,
                BigDecimal.ZERO.setScale(2)
            )
        }
    }

    private fun getActivitiesDuration(activity: List<Activity>): BigDecimal =
        BigDecimal(this.getDurationByCountingNumberOfDays(activity, 1)).divide(
            MINUTES_IN_HOUR, 10, RoundingMode.HALF_UP
        ).setScale(2, RoundingMode.DOWN)

    @Transactional
    @ReadOnly
    fun getSumActivitiesDuration(timeInterval: TimeInterval, projectRoleId: Long, userId: Long) =
        getSumActivitiesDuration(createCalendar(timeInterval.getDateInterval()), timeInterval, projectRoleId, userId)

    @Transactional
    @ReadOnly
    fun getSumActivitiesDuration(
        calendar: Calendar, timeInterval: TimeInterval, projectRoleId: Long, userId: Long
    ): Int {
        val activitiesIntervals = activityService.getActivitiesIntervals(
            timeInterval, projectRoleId, userId
        )
        return if (activitiesIntervals.isEmpty()) {
            0
        } else {
            activitiesIntervals.sumOf {
                getDurationByCountingWorkingDays(
                    TimeInterval.of(it.start, it.end), it.timeUnit, calendar.getWorkableDays(
                        DateInterval.of(it.start.toLocalDate(), it.end.toLocalDate())
                    )
                )
            }
        }
    }

    @Transactional
    @ReadOnly
    fun getDurationByCountingWorkingDays(timeInterval: TimeInterval, projectRole: ProjectRole) =
        getDurationByCountingWorkingDays(timeInterval, projectRole.timeUnit)

    @Transactional
    @ReadOnly
    fun getDurationByCountingWorkingDays(timeInterval: TimeInterval, timeUnit: TimeUnit): Int {
        val calendar = calendarFactory.create(timeInterval.getDateInterval())
        return getDurationByCountingWorkingDays(timeInterval, timeUnit, calendar.workableDays)
    }

    fun getDurationByCountingWorkingDays(
        calendar: Calendar, timeInterval: TimeInterval, projectRole: ProjectRole
    ) = getDurationByCountingWorkingDays(
        timeInterval, projectRole.timeUnit, calendar.getWorkableDays(timeInterval.getDateInterval())
    )

    fun getDurationByCountingWorkingDays(
        timeInterval: TimeInterval, timeUnit: TimeUnit, workableDays: List<LocalDate>
    ) = getDurationByCountingNumberOfDays(timeInterval, timeUnit, workableDays.size)

    fun getDurationByCountingNumberOfDays(activities: List<Activity>, numberOfDays: Int) =
        activities.sumOf { getDurationByCountingNumberOfDays(it, numberOfDays) }

    fun getDurationByCountingNumberOfDays(activity: Activity, numberOfDays: Int) =
        getDurationByCountingNumberOfDays(activity.getTimeInterval(), activity.projectRole.timeUnit, numberOfDays)

    fun getDurationByCountingNumberOfDays(timeInterval: TimeInterval, timeUnit: TimeUnit, numberOfDays: Int) =
        if (timeUnit == TimeUnit.MINUTES) {
            timeInterval.getDuration().toMinutes().toInt()
        } else {
            numberOfDays * 8 * 60
        }

    private companion object {
        private val MINUTES_IN_HOUR = BigDecimal(60)
    }
}