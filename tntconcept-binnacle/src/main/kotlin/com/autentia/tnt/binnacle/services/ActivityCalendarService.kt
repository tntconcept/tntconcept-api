package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DailyWorkingTime
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.DateInterval
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
    private val projectRoleService: ProjectRoleService,
    private val calendarFactory: CalendarFactory,
    private val activitiesCalendarFactory: ActivitiesCalendarFactory,
) {
    @Transactional
    @ReadOnly
    fun getActivityDurationSummaryInHours(dateInterval: DateInterval, userId: Long): List<DailyWorkingTime> {

        val activitiesCalendar = activitiesCalendarFactory.create(dateInterval)
        activitiesCalendar.addAllActivities(activityService.getActivitiesBetweenDates(dateInterval, userId))
        return activitiesCalendar.activitiesCalendarMap.toList().map {
            DailyWorkingTime(
                it.first,
                BigDecimal(this.getDurationByCountingNumberOfDays(it.second, 1)).divide(
                    BigDecimal(60), 10, RoundingMode.HALF_UP
                ).setScale(2, RoundingMode.DOWN)
            )
        }
    }

    @Transactional
    @ReadOnly
    fun getDurationByCountingWorkingDays(timeInterval: TimeInterval, projectRoleId: Long): Int {
        val projectRole = projectRoleService.getByProjectRoleId(projectRoleId)
        return getDurationByCountingWorkingDays(timeInterval, projectRole.timeUnit)
    }

    @Transactional
    @ReadOnly
    fun getDurationByCountingWorkingDays(timeInterval: TimeInterval, timeUnit: TimeUnit): Int {
        val calendar = calendarFactory.create(timeInterval.getDateInterval())
        return getDurationByCountingWorkingDays(timeInterval, timeUnit, calendar.workableDays)
    }

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
}