package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.time.LocalDate

class CalendarTest {
    
    private val holidayService: HolidayService = mock<HolidayService>()
    private val calendarFactory: CalendarFactory = CalendarFactory(holidayService)

    private val date = LocalDate.of(2023, 3, 15)
    private val datePlusOneDay = LocalDate.of(2023, 3, 15).plusDays(1)
    private val datePlusOneWeek = LocalDate.of(2023, 3, 15).plusWeeks(1)
    private val dateSaturday = LocalDate.of(2023, 3, 18)
    private val dateSunday = LocalDate.of(2023, 3, 19)
    private val holiday = Holiday(1, "Holiday description", date.atStartOfDay())
    private val holidayPlusOneDay = Holiday(2, "Holiday description", datePlusOneDay.atStartOfDay())

    @Test
    fun `workableDays with same start, end and holiday dates`() {
        doReturn(listOf(holiday)).whenever(holidayService).findAllBetweenDate(date, date)
        val calendar = calendarFactory.create(DateInterval.of(date, date))
        assert(calendar.workableDays.isEmpty())
    }

    @Test
    fun `workableDays should return one day`() {
        doReturn(listOf(holiday)).whenever(holidayService).findAllBetweenDate(date, datePlusOneDay)
        val calendar = calendarFactory.create(DateInterval.of(date, datePlusOneDay))
        assertEquals(listOf(datePlusOneDay), calendar.workableDays)
    }

    @Test
    fun `workableDays with weekend should return empty list`() {
        val calendar = calendarFactory.create(DateInterval.of(dateSaturday, dateSunday))
        assert(calendar.workableDays.isEmpty())
    }

    @Test
    fun `workableDays with a week with weekend and two holidays`() {
        doReturn(listOf(holiday, holidayPlusOneDay)).whenever(holidayService).findAllBetweenDate(date, datePlusOneWeek)
        val calendar = calendarFactory.create(DateInterval.of(date, datePlusOneWeek))
        assertEquals(4, calendar.workableDays.size)
    }

    @Test
    fun `allDays with same start and end dates`() {
        val calendar = calendarFactory.create(DateInterval.of(date, date))
        assertEquals(listOf(date), calendar.allDays)
    }

    @Test
    fun `allDays with end date previous start date`() {
        val calendar = calendarFactory.create(DateInterval.of(datePlusOneDay, date))
        assert(calendar.allDays.isEmpty())
    }

    @Test
    fun `allDays should returns two days`() {
        val calendar = calendarFactory.create(DateInterval.of(date, datePlusOneDay))
        assertEquals(listOf(date, datePlusOneDay), calendar.allDays)
    }

    @Test
    fun `allDays with dates that differ by on week should returns 8 days`() {
        val calendar = calendarFactory.create(DateInterval.of(date, datePlusOneWeek))
        assertEquals(date, calendar.allDays.first())
        assertEquals(datePlusOneWeek, calendar.allDays.last())
        assertEquals(8, calendar.allDays.size)
    }
}