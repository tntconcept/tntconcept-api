package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDateTime

class ActivityTest {
    private val dateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25)
    private val dateTimePlusOneHour = dateTime.plusHours(1L)

    private val holidayService = mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
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
                ApprovalState.NA
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
                ApprovalState.NA
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
                ApprovalState.NA
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
                ApprovalState.NA
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
                ApprovalState.NA
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
                ApprovalState.NA
            ).getDurationByCountingWorkableDays(calendar)
        )
    }
}