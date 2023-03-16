package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.DateInterval
import com.autentia.tnt.binnacle.services.HolidayService
import jakarta.inject.Singleton

@Singleton
internal class CalendarFactory(private val holidayService: HolidayService) {

    fun create(dateInterval: DateInterval): Calendar {
        val holidays = holidayService.findAllBetweenDate(dateInterval.start, dateInterval.end)
        return Calendar(dateInterval, holidays)
    }
}