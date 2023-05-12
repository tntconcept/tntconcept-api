package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class CalendarWorkableDaysUseCase internal constructor(private val calendarFactory: CalendarFactory) {

    fun get(startDate: LocalDate, endDate: LocalDate) =
        calendarFactory.create(DateInterval.of(startDate, endDate)).workableDays.size

}
