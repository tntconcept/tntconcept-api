package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.ActivityInterval
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ActivityCalendarServiceTest {

    private val activityService = mock<ActivityService>()
    private val projectRoleService = mock<ProjectRoleService>()
    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val activitiesCalendarFactory = ActivitiesCalendarFactory(calendarFactory)

    private val activityCalendarService =
        ActivityCalendarService(activityService, calendarFactory, activitiesCalendarFactory)

    private val dateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25)
    private val dateTimePlusOneHour = dateTime.plusHours(1L)
    private val dateTimePlusTwoDays = dateTimePlusOneHour.plusDays(2)

    private val todayDateTime = LocalDateTime.of(2023, 3, 21, 0, 0)
    private val todayDateTimePlusOneMonth = todayDateTime.plusMonths(1)
    private val lastYearDateTime = todayDateTime.minusMonths(3)
    private val lastYearDateTimePlusOneMonth = lastYearDateTime.plusMonths(1)

    private val date = dateTime.toLocalDate()
    private val datePlusTwoDays = dateTimePlusTwoDays.toLocalDate()

    private val projectRole = createProjectRole().copy(timeUnit = TimeUnit.DAYS)
    private val user = createUser()
    private val activityInMinutes = createDomainActivity()
    private val activityWithDecimals =
        activityInMinutes.copy(start = dateTime, end = dateTime.plusMinutes(80))
    private val activityInDays =
        activityInMinutes.copy(
            start = LocalDateTime.of(2023, 4, 3, 0, 0, 0),
            end = LocalDateTime.of(2023, 4, 4, 0, 0, 0),
            projectRole = ProjectRole(2L, TimeUnit.DAYS)
        )
    private val activities = listOf(activityInMinutes, activityWithDecimals, activityInDays)

    private val monthLongActivity = ActivityInterval(
        todayDateTime,
        todayDateTimePlusOneMonth,
        TimeUnit.DAYS
    )

    private val lastYearActivity = ActivityInterval(
        lastYearDateTime,
        lastYearDateTimePlusOneMonth,
        TimeUnit.DAYS
    )

    @Test
    fun `getActivityDurationSummaryInHours dateInterval, userId should return empty summary with all dates and workedTime set to zero`() {

        val expectedDuration = BigDecimal.ZERO.setScale(2)
        val timeSummary =
            activityCalendarService.getActivityDurationSummaryInHours(
                emptyList(),
                DateInterval.of(date, datePlusTwoDays)
            )

        assertEquals(date, timeSummary[0].date)
        assertEquals(expectedDuration, timeSummary[0].workedHours)

        assertEquals(date.plusDays(1), timeSummary[1].date)
        assertEquals(expectedDuration, timeSummary[1].workedHours)

        assertEquals(datePlusTwoDays, timeSummary[2].date)
        assertEquals(expectedDuration, timeSummary[2].workedHours)
    }

    @Test
    fun `getActivityDurationSummaryInHours dateInterval, userId`() {

        val timeSummary =
            activityCalendarService.getActivityDurationSummaryInHours(
                activities, DateInterval.of(date, todayDateTime.plusWeeks(2).toLocalDate())
            )

        assertEquals(date, timeSummary[0].date)
        assertEquals(BigDecimal("2.33"), timeSummary[0].workedHours)

        assertEquals(LocalDate.of(2023, 4, 3), timeSummary[33].date)
        assertEquals(BigDecimal("8.00"), timeSummary[33].workedHours)

        assertEquals(LocalDate.of(2023, 4, 4), timeSummary[34].date)
        assertEquals(BigDecimal("8.00"), timeSummary[34].workedHours)
    }

    @Test
    fun getActivityDurationByMonth() {
        val activityDurationByMonth =
            activityCalendarService.getActivityDurationByMonth(activities, DateInterval.ofYear(2023))

        val expectedResult: Map<Month, Duration> =
            mapOf(
                Month.MARCH to 2.toDuration(DurationUnit.HOURS) + 20.toDuration(DurationUnit.MINUTES),
                Month.APRIL to 16.toDuration(DurationUnit.HOURS),
            )

        assertEquals(expectedResult, activityDurationByMonth)
    }

    @Test
    fun getActivityDurationByMonthlyRoles() {
        val activityDurationByMonthlyRoles =
            activityCalendarService.getActivityDurationByMonthlyRoles(activities, DateInterval.ofYear(2023))

        val expectedResult: Map<Month, List<MonthlyRoles>> =
            mapOf(
                Month.MARCH to listOf(
                    MonthlyRoles(1, 2.toDuration(DurationUnit.HOURS) + 20.toDuration(DurationUnit.MINUTES)),
                ),
                Month.APRIL to listOf(MonthlyRoles(2, 16.toDuration(DurationUnit.HOURS)))
            )

        assertEquals(activityDurationByMonthlyRoles, expectedResult)
    }

    @Test
    fun getZeroDuration() {
        val timeInterval = TimeInterval.ofYear(2023)

        doReturn(emptyList<ActivityInterval>()).whenever(activityService)
            .getActivitiesIntervals(timeInterval, projectRole.id, user.id)

        val actualDuration = activityCalendarService.getSumActivitiesDuration(timeInterval, projectRole.id, user.id)

        assertEquals(0, actualDuration)
    }

    @Test
    fun getDuration() {
        val timeInterval = TimeInterval.ofYear(2023)
        doReturn(listOf(monthLongActivity, lastYearActivity)).whenever(activityService)
            .getActivitiesIntervals(timeInterval, projectRole.id, user.id)

        val actualDuration = activityCalendarService.getSumActivitiesDuration(timeInterval, projectRole.id, user.id)

        assertEquals(18720, actualDuration)
    }

    @Test
    fun `getDurationByCountingWorkingDays by passing timeInterval, projectRoleId`() {

        doReturn(projectRole).whenever(projectRoleService).getByProjectRoleId(projectRole.id)

        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            TimeInterval.of(dateTime, dateTimePlusTwoDays), projectRole.timeUnit
        )
        assertEquals(1440, duration)
    }

    @Test
    fun `getDurationByCountingWorkingDays by passing timeInterval, timeUnit`() {
        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            TimeInterval.of(dateTime, dateTimePlusTwoDays),
            TimeUnit.DAYS
        )
        assertEquals(1440, duration)
    }

    @Test
    fun `getDurationByCountingWorkingDays by passing timeInterval, timeUnit, workableDays`() {
        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            TimeInterval.of(dateTime, dateTimePlusTwoDays),
            TimeUnit.DAYS,
            listOf(date, datePlusTwoDays)
        )
        assertEquals(960, duration)
    }

    @Test
    fun `getDurationByCountingNumberOfDays by passing activities, numberOfDays`() {
        val duration = activityCalendarService.getDurationByCountingNumberOfDays(activities, 1)
        assertEquals(620, duration)
    }

    @Test
    fun `getDurationByCountingNumberOfDays by passing activity, numberOfDays`() {
        val duration = activityCalendarService.getDurationByCountingNumberOfDays(activityInMinutes, 1)
        assertEquals(60, duration)
    }

    @Test
    fun `getDurationByCountingNumberOfDays in minutes by passing activity, numberOfDays `() {
        val duration = activityCalendarService.getDurationByCountingNumberOfDays(
            TimeInterval.of(dateTime, dateTimePlusOneHour), TimeUnit.MINUTES, 1
        )
        assertEquals(60, duration)
    }

    @Test
    fun `getDurationByCountingNumberOfDays in days by passing timeInterval, timeUnit, numberOfDays `() {
        val duration = activityCalendarService.getDurationByCountingNumberOfDays(
            TimeInterval.of(dateTime, dateTimePlusTwoDays), TimeUnit.DAYS, 1
        )
        assertEquals(480, duration)
    }
}