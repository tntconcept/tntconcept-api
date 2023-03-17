package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.entities.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDateTime

class ActivityCalendarServiceTest {

    private val activityService = mock<ActivityService>()
    private val projectRoleService = mock<ProjectRoleService>()
    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val activitiesCalendarFactory = ActivitiesCalendarFactory(calendarFactory)

    private val activityCalendarService =
        ActivityCalendarService(activityService, projectRoleService, calendarFactory, activitiesCalendarFactory)

    private val dateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25)
    private val dateTimePlusOneHour = dateTime.plusHours(1L)
    private val dateTimePlusTwoDays = dateTimePlusOneHour.plusDays(2)

    private val date = dateTime.toLocalDate()
    private val datePlusTwoDays = dateTimePlusTwoDays.toLocalDate()
    private val projectRole = createProjectRole().copy(timeUnit = TimeUnit.DAYS)
    private val user = createUser()
    private val activityInMinutes = createActivity()
    private val activityWithDecimals =
        activityInMinutes.copy(start = dateTime, end = dateTime.plusMinutes(80), duration = 80)
    private val activityInDays =
        activityInMinutes.copy(
            start = dateTime,
            end = dateTimePlusTwoDays,
            duration = 960,
            projectRole = projectRole
        )
    private val activities = listOf(activityInMinutes, activityWithDecimals, activityInDays)

    @Test
    fun `getActivityDurationSummaryInHours dateInterval, userId`() {
        doReturn(activities).whenever(activityService)
            .getActivitiesBetweenDates(DateInterval.of(date, datePlusTwoDays), user.id)

        val timeSummary =
            activityCalendarService.getActivityDurationSummaryInHours(DateInterval.of(date, datePlusTwoDays), user.id)

        assertEquals(date, timeSummary[0].date)
        assertEquals(0, BigDecimal("10.33").compareTo(timeSummary[0].workedHours))

        assertEquals(date.plusDays(1), timeSummary[1].date)
        assertEquals(0, BigDecimal(8).compareTo(timeSummary[1].workedHours))

        assertEquals(datePlusTwoDays, timeSummary[2].date)
        assertEquals(0, BigDecimal(8).compareTo(timeSummary[2].workedHours))
    }

    @Test
    fun `getDurationByCountingWorkingDays by passing timeInterval, projectRoleId`() {

        doReturn(projectRole).whenever(projectRoleService).getByProjectRoleId(projectRole.id)

        val duration = activityCalendarService.getDurationByCountingWorkingDays(
            TimeInterval.of(dateTime, dateTimePlusTwoDays), projectRole.id
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