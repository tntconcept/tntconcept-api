package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.config.createHoliday
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime

class CalendarTest {

    private val holidayRepository = mock<HolidayRepository>()
    private val calendarFactory: CalendarFactory = CalendarFactory(holidayRepository)

    private val date = LocalDate.of(2023, 3, 15)
    private val datePlusOneDay = LocalDate.of(2023, 3, 15).plusDays(1)
    private val datePlusOneWeek = LocalDate.of(2023, 3, 15).plusWeeks(1)
    private val dateSaturday = LocalDate.of(2023, 3, 18)
    private val dateSunday = LocalDate.of(2023, 3, 19)
    private val holiday = createHoliday()
    private val holidayPlusOneDay = Holiday(2, "Holiday description", datePlusOneDay.atStartOfDay())

    @Test
    fun `workableDays with same start, end and holiday dates`() {
        doReturn(listOf(holiday)).whenever(holidayRepository).findAllByDateBetween(date.atTime(LocalTime.MIN), date.atTime(23, 59, 59))
        val calendar = calendarFactory.create(DateInterval.of(date, date))
        assert(calendar.workableDays.isEmpty())
    }

    @Test
    fun `workableDays should return one day`() {
        doReturn(listOf(holiday)).whenever(holidayRepository).findAllByDateBetween(date.atTime(LocalTime.MIN), datePlusOneDay.atTime(23, 59, 59))
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
        doReturn(listOf(holiday, holidayPlusOneDay)).whenever(holidayRepository).findAllByDateBetween(date.atTime(
            LocalTime.MIN), datePlusOneWeek.atTime(23, 59, 59))
        val calendar = calendarFactory.create(DateInterval.of(date, datePlusOneWeek))
        assertEquals(4, calendar.workableDays.size)
    }

    @Test
    fun getWorkableDays() {
        val calendar = calendarFactory.create(DateInterval.of(date, datePlusOneWeek))
        assertEquals(listOf(date, datePlusOneDay), calendar.getWorkableDays(DateInterval.of(date, datePlusOneDay)))
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