package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO

data class HolidayResponse(
    val holidays: List<HolidayDetailsResponse>,
    val vacations: List<VacationResponse>,
) {
    companion object {
        fun from(holidayResponseDTO: HolidayResponseDTO): HolidayResponse =
            HolidayResponse(
                holidayResponseDTO.holidays.map { HolidayDetailsResponse.from(it) },
                holidayResponseDTO.vacations.map { VacationResponse.from(it) }
            )
    }
}
