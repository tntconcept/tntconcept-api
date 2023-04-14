package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.core.utils.isHoliday
import com.autentia.tnt.binnacle.core.utils.isWeekend
import com.autentia.tnt.binnacle.core.utils.myDatesUntil
import com.autentia.tnt.binnacle.entities.Holiday
import java.time.LocalDate

class Calendar(private val dateInterval: DateInterval, private val holidays: List<Holiday>) {

    val workableDays: List<LocalDate> by lazy {
        val holidaysDates = holidays.map { it.date.toLocalDate() }
        allDays.filterNot { it.isWeekend() || it.isHoliday(holidaysDates) }
    }

    val allDays: List<LocalDate> by lazy {
        dateInterval.start.myDatesUntil(dateInterval.end).toList()
    }

    fun getWorkableDays(dateInterval: DateInterval) = workableDays.filter { dateInterval.includes(it) }
}