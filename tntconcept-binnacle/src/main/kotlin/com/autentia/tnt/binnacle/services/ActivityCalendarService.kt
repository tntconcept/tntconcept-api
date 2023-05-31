package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.ActivityTimeInterval
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DailyWorkingTime
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month
import javax.transaction.Transactional
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Singleton
internal class ActivityCalendarService(
    private val calendarFactory: CalendarFactory,
    private val activitiesCalendarFactory: ActivitiesCalendarFactory,
) {

    @Transactional
    @ReadOnly
    fun createCalendar(dateInterval: DateInterval) = calendarFactory.create(dateInterval)

    @Transactional
    @ReadOnly
    fun getActivityDurationSummaryInHours(
        activities: List<Activity>,
        dateInterval: DateInterval
    ): List<DailyWorkingTime> {
        val activitiesCalendarMap = getActivityCalendarMap(activities, dateInterval)
        return activitiesCalendarMap.toList().map {
            toDailyWorkingTime(it)
        }
    }

    private fun toDailyWorkingTime(activitiesInDate: Pair<LocalDate, List<Activity>>): DailyWorkingTime {
        return if (activitiesInDate.second.isNotEmpty()) {
            DailyWorkingTime(
                activitiesInDate.first,
                getActivitiesDurationInHours(activitiesInDate.second)
            )
        } else {
            DailyWorkingTime(
                activitiesInDate.first,
                BigDecimal.ZERO.setScale(2)
            )
        }
    }

    private fun getActivitiesDurationInHours(activity: List<Activity>): BigDecimal =
        BigDecimal(this.getDurationByCountingNumberOfDays(activity, 1)).divide(
            MINUTES_IN_HOUR, 10, RoundingMode.HALF_UP
        ).setScale(2, RoundingMode.DOWN)

    @Transactional
    @ReadOnly
    fun getActivityDurationByMonth(
        activities: List<Activity>, dateInterval: DateInterval
    ): Map<Month, Duration> {
        val activityCalendarMap = getActivityCalendarMap(activities, dateInterval)
        return activityCalendarMap.toList().filter { it.second.isNotEmpty() }.groupBy { it.first.month }.mapValues {
            getActivitiesDuration(it.value.flatMap { dateActivityPair -> dateActivityPair.second })
        }
    }

    @Transactional
    @ReadOnly
    fun getActivityDurationByMonthlyRoles(
        activities: List<Activity>,
        dateInterval: DateInterval
    ): Map<Month, List<MonthlyRoles>> {
        val activityCalendarMap = getActivityCalendarMap(activities, dateInterval)
        return activityCalendarMap.toList().filter { it.second.isNotEmpty() }.groupBy { it.first.month }.mapValues {
            it.value.flatMap { dateActivityPair -> dateActivityPair.second }
                .groupBy { activity -> activity.projectRole.id }
                .map { projectRole -> toMonthlyRoles(projectRole.key, projectRole.value) }
        }
    }

    private fun toMonthlyRoles(projectRoleId: Long, activities: List<Activity>) =
        MonthlyRoles(projectRoleId, getActivitiesDuration(activities))

    private fun getActivitiesDuration(activities: List<Activity>) =
        this.getDurationByCountingNumberOfDays(activities, 1).minutes

    @Transactional
    @ReadOnly
    fun getActivityCalendarMap(activities: List<Activity>, dateInterval: DateInterval): Map<LocalDate, List<Activity>> {
        val activitiesCalendar = activitiesCalendarFactory.create(dateInterval)
        activitiesCalendar.addAllActivities(activities)
        return activitiesCalendar.activitiesCalendarMap
    }

    fun getRemainingOfProjectRoleForUser(projectRole: ProjectRole, activities: List<Activity>, dateInterval: DateInterval, userId: Long): Int{
        val calendar = createCalendar(dateInterval)
        return projectRole.getRemainingInUnits(calendar, filterActivities(activities, projectRole, userId))
    }

    private fun filterActivities(
        activities: List<Activity>,
        projectRole: ProjectRole,
        userId: Long
    ) = activities.filter { it.projectRole == projectRole && it.userId == userId }

    @Transactional
    @ReadOnly
    fun getDurationByCountingWorkingDays(activity: ActivityTimeInterval): Int {
        val calendar = calendarFactory.create(activity.getDateInterval())
        return activity.getDurationByCountingWorkableDays(calendar)
    }

    fun getDurationByCountingNumberOfDays(activities: List<Activity>, numberOfDays: Int) =
        activities.sumOf { getDurationByCountingNumberOfDays(it, numberOfDays) }

    fun getDurationByCountingNumberOfDays(activity: Activity, numberOfDays: Int) =
        activity.getDurationByCountingDays(numberOfDays)

    private companion object {
        private val MINUTES_IN_HOUR = BigDecimal(60)
    }
}