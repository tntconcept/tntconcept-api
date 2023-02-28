package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.services.HolidayService
import com.autentia.tnt.binnacle.services.VacationService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import javax.transaction.Transactional

@Singleton
class PrivateHolidaysPeriodDaysUseCase internal constructor(
    private val holidayService: HolidayService,
    private val vacationService: VacationService
) {
    @Transactional
    @ReadOnly
    fun get(startDate: LocalDate, endDate: LocalDate): Int {
        val publicHolidays = holidayService.findAllBetweenDate(startDate, endDate)
        return vacationService.getVacationPeriodDays(startDate, endDate, publicHolidays).size
    }

}
