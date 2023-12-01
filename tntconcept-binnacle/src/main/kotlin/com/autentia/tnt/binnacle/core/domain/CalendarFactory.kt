package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime

@Singleton
internal class CalendarFactory(
    private val holidayRepository: HolidayRepository,
) {

    fun create(dateInterval: DateInterval): Calendar {
        val holidays = getIntervalHolidays(dateInterval)
        return Calendar(dateInterval, holidays)
    }

    private fun getIntervalHolidays(dateInterval: DateInterval): List<Holiday> {
        val startDateMinHour = dateInterval.start.atTime(LocalTime.MIN)
        val endDateMaxHour = dateInterval.end.atTime(23, 59, 59)
        return holidayRepository.findAllByDateBetween(startDateMinHour, endDateMaxHour)
    }
}