package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.HolidayConverter
import com.autentia.tnt.binnacle.converters.HolidayResponseConverter
import com.autentia.tnt.binnacle.core.domain.HolidaysResponse
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import com.autentia.tnt.binnacle.entities.dto.HolidaysResponseDTO
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.services.VacationService
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import com.autentia.tnt.binnacle.core.domain.Vacation as VacationDomain

@Singleton
class UserHolidayBetweenDatesUseCase internal constructor(
    private val holidayRepository: HolidayRepository,
    private val holidayConverter: HolidayConverter
) {

    fun getHolidays(year: Int?): List<HolidayDTO> {
        val startDate: LocalDate = LocalDate.of(year ?: LocalDate.now().year, 1, 1)
        val endDate: LocalDate = LocalDate.of(year ?: LocalDate.now().year, 12, 31)

        val holidays = getHolidaysBetweenDates(startDate, endDate)

        return holidays.map { holidayConverter.toHolidayDTO(it) }
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
