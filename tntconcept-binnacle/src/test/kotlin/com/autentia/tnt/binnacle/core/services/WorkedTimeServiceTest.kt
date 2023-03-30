package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.ProjectRole
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.Month
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class WorkedTimeServiceTest {

    private lateinit var workableProjectRoleIdChecker: WorkableProjectRoleIdChecker
    private lateinit var activityCalendarService: ActivityCalendarService
    private lateinit var sut: WorkedTimeService
    private lateinit var activityService: ActivityService
    private lateinit var holidayService: HolidayService
    private lateinit var calendarFactory: CalendarFactory

    @BeforeEach
    fun setUp() {
        workableProjectRoleIdChecker = mock()
        activityService = mock()
        holidayService = mock()
        calendarFactory = CalendarFactory(holidayService)
        activityCalendarService =
            ActivityCalendarService(activityService, calendarFactory, ActivitiesCalendarFactory(calendarFactory))
        sut = WorkedTimeService(activityCalendarService, workableProjectRoleIdChecker)
    }

    @Test
    fun `should return worked time excluding not workable project roles time`() {
        val hours = 8L
        val workableProjectRole = ProjectRole(1, TimeUnit.MINUTES)
        val notWorkableProjectRole = ProjectRole(2, TimeUnit.MINUTES)
        val activities = listOf(
            Activity(
                LocalDateTime.parse("2021-01-01T10:00:00"),
                LocalDateTime.parse("2021-01-01T10:00:00").plusHours(hours),
                workableProjectRole
            ),
            Activity(
                LocalDateTime.parse("2021-01-02T10:00:00"),
                LocalDateTime.parse("2021-01-02T10:00:00").plusHours(hours),
                workableProjectRole
            ),
            Activity(
                LocalDateTime.parse("2021-02-01T10:00:00"),
                LocalDateTime.parse("2021-02-01T10:00:00").plusHours(hours),
                workableProjectRole
            ),
            Activity(
                LocalDateTime.parse("2021-02-03T10:00:00"),
                LocalDateTime.parse("2021-02-03T10:00:00").plusHours(hours),
                notWorkableProjectRole
            )
        )
        whenever(workableProjectRoleIdChecker.isWorkable(ProjectRoleId(workableProjectRole.id))).thenReturn(true)
        whenever(workableProjectRoleIdChecker.isWorkable(ProjectRoleId(notWorkableProjectRole.id))).thenReturn(false)

        val expectedResult: Map<Month, Duration> =
            mapOf(
                Month.JANUARY to 16.toDuration(DurationUnit.HOURS),
                Month.FEBRUARY to 8.toDuration(DurationUnit.HOURS),
            )

        val workedTime = sut.workedTime(DateInterval.ofYear(2021), eq(activities))

        assertEquals(expectedResult, workedTime)
    }
}
