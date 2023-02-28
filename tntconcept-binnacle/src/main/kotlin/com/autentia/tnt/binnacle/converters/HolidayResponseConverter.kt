package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.HolidayResponse
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import jakarta.inject.Singleton

@Singleton
class HolidayResponseConverter(
    private val vacationConverter: VacationConverter,
    private val holidayConverter: HolidayConverter
) {

    fun toHolidayResponseDTO(holidayResponse: HolidayResponse) =
        HolidayResponseDTO(
            holidays = holidayResponse.holidays.map { holidayConverter.toHolidayDTO(it) },
            vacations = holidayResponse.vacations.map { vacationConverter.toVacationDTO(it) }
        )

}
