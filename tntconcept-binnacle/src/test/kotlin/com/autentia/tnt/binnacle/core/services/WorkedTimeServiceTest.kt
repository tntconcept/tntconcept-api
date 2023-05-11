package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.core.domain.ActivitiesCalendarFactory
import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.domain.TimeInterval
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

    private val hours = 8L
    private val projectRole = createDomainProjectRole()
    private val otherProjectRole = createDomainProjectRole().copy(id = 2L)
    private val projectRoleInDays = createDomainProjectRole().copy(id = 3L, timeUnit = TimeUnit.DAYS)
    private val activities = listOf(
        createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.parse("2021-01-01T10:00:00"),
                LocalDateTime.parse("2021-01-01T10:00:00").plusHours(hours)
            ),
            projectRole = projectRole
        ),
        createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.parse("2021-01-02T10:00:00"),
                LocalDateTime.parse("2021-01-02T10:00:00").plusHours(hours)
            ),
            projectRole = projectRole
        ),
        createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.parse("2021-02-01T10:00:00"),
                LocalDateTime.parse("2021-02-01T10:00:00").plusHours(hours)
            ),
            projectRole = projectRole
        ),
        createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.parse("2021-02-03T10:00:00"),
                LocalDateTime.parse("2021-02-03T10:00:00").plusHours(hours)
            ),
            projectRole = otherProjectRole
        ),
        createDomainActivity().copy(
            timeInterval = TimeInterval.of(
                LocalDateTime.parse("2021-03-15T00:00:00"),
                LocalDateTime.parse("2021-03-15T00:00:00").plusMonths(1L)
            ),
            projectRole = projectRoleInDays
        ),
    )

    @BeforeEach
    fun setUp() {
        workableProjectRoleIdChecker = mock()
        activityService = mock()
        holidayService = mock()
        calendarFactory = CalendarFactory(holidayService)
        activityCalendarService =
            ActivityCalendarService(calendarFactory, ActivitiesCalendarFactory(calendarFactory))
        sut = WorkedTimeService(activityCalendarService, workableProjectRoleIdChecker)
    }

    @Test
    fun `should return worked time excluding not workable project roles time`() {

        whenever(workableProjectRoleIdChecker.isWorkable(ProjectRoleId(projectRole.id))).thenReturn(true)
        whenever(workableProjectRoleIdChecker.isWorkable(ProjectRoleId(projectRoleInDays.id))).thenReturn(true)
        whenever(workableProjectRoleIdChecker.isWorkable(ProjectRoleId(otherProjectRole.id))).thenReturn(false)

        val expectedResult: Map<Month, Duration> =
            mapOf(
                Month.JANUARY to 16.toDuration(DurationUnit.HOURS),
                Month.FEBRUARY to 8.toDuration(DurationUnit.HOURS),
                Month.MARCH to 104.toDuration(DurationUnit.HOURS),
                Month.APRIL to 88.toDuration(DurationUnit.HOURS),
            )

        val workedTime = sut.workedTime(DateInterval.ofYear(2021), eq(activities))

        assertEquals(expectedResult, workedTime)
    }

    @Test
    fun `should return worked time excluding not workable project roles time grouped by project role`() {

        whenever(workableProjectRoleIdChecker.isWorkable(ProjectRoleId(projectRole.id))).thenReturn(true)
        whenever(workableProjectRoleIdChecker.isWorkable(ProjectRoleId(projectRoleInDays.id))).thenReturn(true)
        whenever(workableProjectRoleIdChecker.isWorkable(ProjectRoleId(otherProjectRole.id))).thenReturn(true)

        val expectedResult: Map<Month, List<MonthlyRoles>> =
            mapOf(
                Month.JANUARY to listOf(MonthlyRoles(1, 16.toDuration(DurationUnit.HOURS))),
                Month.FEBRUARY to listOf(
                    MonthlyRoles(1, 8.toDuration(DurationUnit.HOURS)),
                    MonthlyRoles(2, 8.toDuration(DurationUnit.HOURS))
                ),
                Month.MARCH to listOf(MonthlyRoles(3, 104.toDuration(DurationUnit.HOURS))),
                Month.APRIL to listOf(MonthlyRoles(3, 88.toDuration(DurationUnit.HOURS)))
            )

        val workedTime = sut.getWorkedTimeByRoles(DateInterval.ofYear(2021), eq(activities))

        assertEquals(expectedResult, workedTime)
    }
}