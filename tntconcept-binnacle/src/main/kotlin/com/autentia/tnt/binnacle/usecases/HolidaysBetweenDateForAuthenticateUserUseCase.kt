package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.HolidayResponseConverter
import com.autentia.tnt.binnacle.core.domain.HolidayResponse
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.services.VacationService
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain

@Singleton
class HolidaysBetweenDateForAuthenticateUserUseCase internal constructor(
    private val holidayRepository: HolidayRepository,
    private val vacationService: VacationService,
    private val holidayResponseConverter: HolidayResponseConverter
) {

    fun getHolidays(startDate: LocalDate, endDate: LocalDate): HolidayResponseDTO {
        val holidays: List<Holiday> = getHolidaysBetweenDates(startDate, endDate)
        val vacations: List<VacationDomain> = vacationService.getVacationsBetweenDates(startDate, endDate)

        return holidayResponseConverter.toHolidayResponseDTO(HolidayResponse(holidays, vacations))
    }

    private fun getHolidaysBetweenDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Holiday> {
        val startDateMinHour = startDate.atTime(LocalTime.MIN)
        val endDateMaxHour = endDate.atTime(23, 59, 59)
        return holidayRepository.findAllByDateBetween(startDateMinHour, endDateMaxHour)
    }

}
