package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.HolidaysResponse
import com.autentia.tnt.binnacle.entities.dto.HolidaysResponseDTO
import jakarta.inject.Singleton

@Singleton
class HolidayResponseConverter(
    private val vacationConverter: VacationConverter,
    private val holidayConverter: HolidayConverter
) {

    @Deprecated("Use instead toHolidayResponseDTO")
    fun toHolidaysResponseDTO(holidaysResponse: HolidaysResponse) =
        HolidaysResponseDTO(
            holidays = holidaysResponse.holidays.map { holidayConverter.toHolidayDTO(it) },
            vacations = holidaysResponse.vacations.map { vacationConverter.toVacationDTO(it) }
        )
}
