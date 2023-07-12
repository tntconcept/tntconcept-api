package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDateTime

class ActivityTest {
    private val dateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25)
    private val dateTimePlusOneHour = dateTime.plusHours(1L)

    private val holidayRepository = mock<HolidayRepository>()

    private val calendarFactory = CalendarFactory(holidayRepository)
    private val calendar = calendarFactory.create(DateInterval.ofYear(2023))

    @Test
    fun `test isOneDay shold return true`() {
        assertTrue(
            createDomainActivity().copy(timeInterval = TimeInterval.of(dateTime, dateTimePlusOneHour)).isOneDay()
        )
    }

    @Test
    fun `test isOneDay shold return false`() {
        assertFalse(
            createDomainActivity().copy(timeInterval = TimeInterval.of(dateTime, dateTime.plusDays(1))).isOneDay()
        )
    }

    @Test
    fun `test getDurationByCountingDays in minutes `() {
        assertEquals(
            60,
            Activity.of(
                1L,
                TimeInterval.of(dateTime, dateTimePlusOneHour),
                60,
                "Description",
                createDomainProjectRole(),
                1L,
                true,
                null,
                LocalDateTime.now(),
                false,
                ApprovalState.NA,
                null
            ).getDurationByCountingDays(0)
        )
    }

    @Test
    fun `test getDurationByCountingDays in days `() {
        assertEquals(
            480,
            Activity.of(
                1L,
                TimeInterval.of(
                    dateTime,
                    dateTimePlusOneHour
                ),
                60,
                "Description",
                createDomainProjectRole().copy(timeUnit = TimeUnit.DAYS),
                1L,
                true,
                null,
                LocalDateTime.now(),
                false,
                ApprovalState.NA,
                null
            ).getDurationByCountingDays(1)
        )
    }

    @Test
    fun `test getDurationByCountingDays of days should return zero duration `() {
        assertEquals(
            0,
            Activity.of(
                1L,
                TimeInterval.of(dateTime, dateTime),
                60,
                "Description",
                createDomainProjectRole(),
                1L,
                true,
                null,
                LocalDateTime.now(),
                false,
                ApprovalState.NA,
                null
            ).getDurationByCountingDays(1)
        )
    }


    @Test
    fun `test getDurationByCountingWorkableDays in minutes `() {
        assertEquals(
            60,
            Activity.of(
                1L,
                TimeInterval.of(dateTime, dateTimePlusOneHour),
                60,
                "Description",
                createDomainProjectRole(),
                1L,
                true,
                null,
                LocalDateTime.now(),
                false,
                ApprovalState.NA,
                null
            ).getDurationByCountingWorkableDays(calendar)
        )
    }

    @Test
    fun `test getDurationByCountingWorkableDays in days `() {
        assertEquals(
            480,
            Activity.of(
                1L,
                TimeInterval.of(
                    dateTime,
                    dateTimePlusOneHour
                ),
                480,
                "Description",
                createDomainProjectRole().copy(timeUnit = TimeUnit.DAYS),
                1L,
                true,
                null,
                LocalDateTime.now(),
                false,
                ApprovalState.NA,
                null
            ).getDurationByCountingWorkableDays(calendar)
        )
    }


    @Test
    fun `test getDurationByCountingWorkableDays should return zero duration `() {
        assertEquals(
            0,
            Activity.of(
                1L,
                TimeInterval.of(dateTime, dateTime),
                0,
                "Description",
                createDomainProjectRole(),
                1L,
                true,
                null,
                LocalDateTime.now(),
                false,
                ApprovalState.NA,
                null
            ).getDurationByCountingWorkableDays(calendar)
        )
    }
}