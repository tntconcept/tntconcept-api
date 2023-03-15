package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.services.HolidayService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDate

class ActivitiesCalendarTest {

    private val holidayService: HolidayService = Mockito.mock<HolidayService>()
    private val calendarFactory = CalendarFactory(holidayService)
    private val date = LocalDate.of(2023, 3, 15)
    private val datePlusOneDay = LocalDate.of(2023, 3, 15).plusDays(1)
    private val datePlusMonth = LocalDate.of(2023, 3, 15).plusMonths(1)

    @Test
    fun `if no activities were added, there should be no activities on the activitiesCalendarMap`() {

        val calendar = calendarFactory.create(DateInterval.of(date, datePlusMonth))
        val activitiesCalendar = ActivitiesCalendar(calendar)

        assert(activitiesCalendar.activitiesCalendarMap.values.all { it.isEmpty() })
    }
}