package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
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
        assertTrue(createDomainActivity().copy(start = dateTime, end = dateTimePlusOneHour).isOneDay())
    }

    @Test
    fun `test isOneDay shold return false`() {
        assertFalse(createDomainActivity().copy(start = dateTime, end = dateTime.plusDays(1)).isOneDay())
    }

    @Test
    fun `test getDurationByCountingDays in minutes `() {
        assertEquals(
            60,
            Activity(dateTime, dateTimePlusOneHour, createDomainProjectRole(), 1L).getDurationByCountingDays(0)
        )
    }

    @Test
    fun `test getDurationByCountingDays in days `() {
        assertEquals(
            480,
            Activity(
                dateTime,
                dateTimePlusOneHour,
                createDomainProjectRole().copy(timeUnit = TimeUnit.DAYS),
                1L
            ).getDurationByCountingDays(1)
        )
    }

    @Test
    fun `test getDurationByCountingDays of days should return zero duration `() {
        assertEquals(
            0,
            Activity(dateTime, dateTime, createDomainProjectRole(), 1L).getDurationByCountingDays(1)
        )
    }


    @Test
    fun `test getDurationByCountingWorkableDays in minutes `() {
        assertEquals(
            60,
            Activity(dateTime, dateTimePlusOneHour, createDomainProjectRole(), 1L).getDurationByCountingWorkableDays(
                calendar
            )
        )
    }

    @Test
    fun `test getDurationByCountingWorkableDays in days `() {
        assertEquals(
            480,
            Activity(
                dateTime,
                dateTimePlusOneHour,
                createDomainProjectRole().copy(timeUnit = TimeUnit.DAYS),
                1L
            ).getDurationByCountingWorkableDays(calendar)
        )
    }


    @Test
    fun `test getDurationByCountingWorkableDays should return zero duration `() {
        assertEquals(
            0,
            Activity(dateTime, dateTime, createDomainProjectRole(), 1L).getDurationByCountingWorkableDays(
                calendar
            )
        )
    }
}