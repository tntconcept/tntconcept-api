package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import javax.transaction.Transactional

@Singleton
class PrivateHolidaysPeriodDaysUseCase internal constructor(private val calendarFactory: CalendarFactory) {
    @Transactional
    @ReadOnly
    fun get(startDate: LocalDate, endDate: LocalDate) =
        calendarFactory.create(DateInterval.of(startDate, endDate)).workableDays.size
}
