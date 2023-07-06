package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ActivityCalendarServiceTest {

    private val holidayRepository = mock<HolidayRepository>()

    private val calendarFactory = CalendarFactory(holidayRepository)
    private val activitiesCalendarFactory = ActivitiesCalendarFactory(calendarFactory)

    private val activityCalendarService =
        ActivityCalendarService(calendarFactory, activitiesCalendarFactory)

    private val dateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25)
    private val dateTimePlusOneHour = dateTime.plusHours(1L)
    private val dateTimePlusTwoDays = dateTimePlusOneHour.plusDays(2)

    private val todayDateTime = LocalDateTime.of(2023, 3, 21, 0, 0)

    private val date = dateTime.toLocalDate()
    private val datePlusTwoDays = dateTimePlusTwoDays.toLocalDate()
    private val projectRoleInMinutes = createDomainProjectRole().copy(maxAllowed = 480)
    private val activityInMinutes =
        createDomainActivity().copy(projectRole = projectRoleInMinutes)
    private val activityWithDecimals =
        activityInMinutes.copy(
            timeInterval = TimeInterval.of(dateTime, dateTime.plusMinutes(80)),
            projectRole = projectRoleInMinutes,
            userId = 2
        )
    private val activityInDays =
        activityInMinutes.copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.of(2023, 4, 3, 0, 0, 0),
                LocalDateTime.of(2023, 4, 4, 23, 59, 59)
            ),
            projectRole = createDomainProjectRole().copy(id = 2L, timeUnit = TimeUnit.DAYS, maxAllowed = 1440)
        )
    private val activities = listOf(activityInMinutes, activityWithDecimals, activityInDays)

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
    fun `get activity duration by month`() {
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
    fun `should calculate remaining for project role`() {
        assertEquals(
            420, activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRoleInMinutes,
                activities,
                DateInterval.ofYear(2023),
                1L
            )
        )

        assertEquals(
            400, activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRoleInMinutes,
                activities,
                DateInterval.ofYear(2023),
                2L
            )
        )

        assertEquals(
            1, activityCalendarService.getRemainingOfProjectRoleForUser(
                activityInDays.projectRole,
                activities,
                DateInterval.ofYear(2023),
                1L
            )
        )

        assertEquals(
            480, activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRoleInMinutes,
                emptyList(),
                DateInterval.ofYear(2023),
                1L
            )
        )

        assertEquals(
            0, activityCalendarService.getRemainingOfProjectRoleForUser(
                projectRoleInMinutes.copy(maxAllowed = 0),
                emptyList(),
                DateInterval.ofYear(2023),
                1L
            )
        )
    }

    @Test
    fun `get duration counting working days`() {
        val duration = activityCalendarService.getDurationByCountingWorkingDays(activityInMinutes)
        assertEquals(60, duration)
    }

    @Test
    fun `get duration by counting number of days of activities should return the number of days`() {
        val duration = activityCalendarService.getDurationByCountingNumberOfDays(activities, 1)
        assertEquals(620, duration)
    }

    @Test
    fun `get duration by counting number of days of activity should return the number of days`() {
        val duration = activityInMinutes.getDurationByCountingDays(1)
        assertEquals(60, duration)
    }
}