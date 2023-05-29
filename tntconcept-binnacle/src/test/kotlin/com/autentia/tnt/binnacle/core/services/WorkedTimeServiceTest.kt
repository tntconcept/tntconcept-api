package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.MonthlyRoles
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.time.Month
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class WorkedTimeServiceTest {

    private lateinit var activityCalendarService: ActivityCalendarService
    private lateinit var sut: WorkedTimeService

    @BeforeAll
    fun setUp() {
        activityCalendarService = mock()
        sut = WorkedTimeService(activityCalendarService)
    }

    @AfterEach
    fun resetMocks() {
        reset(activityCalendarService)
    }

    @Test
    fun `should return worked time excluding not workable project roles time`() {
        // Given a list of activities for workable and not workable project roles in year 2021
        val dateInterval = DateInterval.ofYear(2021)
        val activities = listOfActivitiesForWorkableAndNotWorkableRoles()

        val fixedResult: Map<Month, Duration> = mapOf(
            Month.JANUARY to 16.toDuration(DurationUnit.HOURS),
            Month.FEBRUARY to 8.toDuration(DurationUnit.HOURS),
            Month.MARCH to 104.toDuration(DurationUnit.HOURS),
            Month.APRIL to 88.toDuration(DurationUnit.HOURS),
        )

        doReturn(fixedResult).whenever(this.activityCalendarService).getActivityDurationByMonth(any(), any())

        // When worked time method is called with the parameters
        sut.workedTime(dateInterval, activities)

        // Then verify the activity calendar service is called only with activities with workable project roles
        verify(activityCalendarService).getActivityDurationByMonth(argThat {
            this.all { it.isWorkingTimeActivity() }
        }, eq(dateInterval))
    }

    @Test
    fun `should return worked time excluding not workable project roles time grouped by project role`() {
        // Given a list of activities for workable and not workable project roles in year 2021
        val dateInterval = DateInterval.ofYear(2021)
        val activities = listOfActivitiesForWorkableAndNotWorkableRoles()

        val fixedResult: Map<Month, List<MonthlyRoles>> = mapOf(
            Month.JANUARY to listOf(MonthlyRoles(1, 16.toDuration(DurationUnit.HOURS))),
            Month.FEBRUARY to listOf(
                MonthlyRoles(1, 8.toDuration(DurationUnit.HOURS)), MonthlyRoles(2, 8.toDuration(DurationUnit.HOURS))
            ),
            Month.MARCH to listOf(MonthlyRoles(3, 104.toDuration(DurationUnit.HOURS))),
            Month.APRIL to listOf(MonthlyRoles(3, 88.toDuration(DurationUnit.HOURS)))
        )

        doReturn(fixedResult).whenever(this.activityCalendarService).getActivityDurationByMonthlyRoles(any(), any())

        // When
        sut.getWorkedTimeByRoles(dateInterval, activities)

        verify(activityCalendarService).getActivityDurationByMonthlyRoles(argThat {
            this.all { it.isWorkingTimeActivity() }
        }, eq(dateInterval))
    }

    private companion object {
        private const val hours = 8L
        private val workableProjectRole = createDomainProjectRole().copy(isWorkingTime = true)
        private val notWorkableProjectRole = createDomainProjectRole().copy(id = 2L, isWorkingTime = false)

        private fun listOfActivitiesForWorkableAndNotWorkableRoles() = listOf(
            createDomainActivity().copy(
                id = 1L, timeInterval = TimeInterval.of(
                    LocalDateTime.parse("2021-01-01T10:00:00"),
                    LocalDateTime.parse("2021-01-01T10:00:00").plusHours(hours)
                ), projectRole = workableProjectRole
            ), createDomainActivity().copy(
                id = 2L, timeInterval = TimeInterval.of(
                    LocalDateTime.parse("2021-01-02T10:00:00"),
                    LocalDateTime.parse("2021-01-02T10:00:00").plusHours(hours)
                ), projectRole = workableProjectRole
            ), createDomainActivity().copy(
                id = 3L, timeInterval = TimeInterval.of(
                    LocalDateTime.parse("2021-02-01T10:00:00"),
                    LocalDateTime.parse("2021-02-01T10:00:00").plusHours(hours)
                ), projectRole = workableProjectRole
            ), createDomainActivity().copy(
                id = 4L, timeInterval = TimeInterval.of(
                    LocalDateTime.parse("2021-02-03T10:00:00"),
                    LocalDateTime.parse("2021-02-03T10:00:00").plusHours(hours)
                ), projectRole = notWorkableProjectRole
            )
        )
    }
}